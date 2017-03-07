package tcslab.syndesiapp.controllers.localization;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import tcslab.syndesiapp.controllers.account.AccountController;
import tcslab.syndesiapp.controllers.automation.AutomationController;
import tcslab.syndesiapp.controllers.ui.WifiReceiver;
import tcslab.syndesiapp.models.Account;
import tcslab.syndesiapp.models.PreferenceKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Perform the WiFi access point scans in order to use the localization classifier.
 *
 * Created by blais on 30.11.2016.
 */
public class WifiService extends IntentService {
    private LocalizationClassifier mLocalizationClassifier;
    private AccountController mAccountController;
    private AutomationController mAutomationController;
    private Handler mHandler;
    private List<List<ScanResult>> mReadings = new ArrayList<>();
    private final Object mLock = new Object();

    public WifiService() {
        super("WifiService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiWakeLock");

        // TODO: Check if the user moved otherwise no need to perform localization

        mLocalizationClassifier = new LocalizationClassifier(this);
        mAccountController = AccountController.getInstance(this);
        mAutomationController = AutomationController.getInstance(this.getApplicationContext());

        /* Load OpenCV */
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "  OpenCVLoader.initDebug(), not working.");
        } else {
            mLocalizationClassifier.mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        // Register the Wifi receiver
        // Register the Broadcast listener
        WifiReceiver wifiReceiver = new WifiReceiver(this);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Get the number of scans from the preferences
        String precision = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKey.PREF_LOC_PRECISION.toString(),"1");

        // Perform the scans
        while(mReadings.size() < Integer.parseInt(precision)) {
            toaster("Launching scan " + (mReadings.size()+1) + " of " + precision);
            ((WifiManager) getSystemService(Context.WIFI_SERVICE)).startScan();

            // Wait for the scan's results
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Log.e("WifiService", "Error while waiting scan results: " + e.getMessage());
                }
            }
        }

        unregisterReceiver(wifiReceiver);

        // Save old location
        String oldOffice = mLocalizationClassifier.getmCurrentPosition();

        // Compute new location
        String newOffice = mLocalizationClassifier.updateLocation(mReadings);

        // When changing office trigger automation
        if(oldOffice != null && !oldOffice.equals(newOffice)){
            mAutomationController.changeOffice(newOffice, oldOffice);
        }

        // Update account office if using Syndesi
        if(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
            Account oldAccount = mAccountController.getAccount();
            oldAccount.setmOffice(newOffice);
            mAccountController.saveAccount(oldAccount);
        }

        // Clear readings for next localization
        mReadings.clear();

        if(wakeLock.isHeld()){
            wakeLock.release();
        }
    }

    private void toaster(String message){
        final String toastMessage = message;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WifiService.this, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toaster(String message, int duration)
    {
        final String toastMessage = message;
        final int toastDuration = duration;

        Boolean permission = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKey.PREF_PERMISION.toString(), false);

        if (permission) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WifiService.this, toastMessage, toastDuration).show();
                }
            });
        }
    }

    public void sendResults(List<ScanResult> readings) {
        mReadings.add(readings);
        synchronized (mLock){
            mLock.notifyAll();
        }
    }
}
