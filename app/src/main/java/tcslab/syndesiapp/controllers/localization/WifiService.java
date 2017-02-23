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
    private Handler mHandler;
    private List<List<ScanResult>> mReadings = new ArrayList<>();
    private final Object mLock = new Object();

    public WifiService() {
        super("WifiListener");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiWakeLock");

        mLocalizationClassifier = new LocalizationClassifier(this);
        mAccountController = AccountController.getInstance(this);

        /* Load OpenCV */
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "  OpenCVLoader.initDebug(), not working.");
        } else {
            mLocalizationClassifier.mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        //Register the Wifi receiver
        //Register the Broadcast listener
        WifiReceiver wifiReceiver = new WifiReceiver(this);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        String precision = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKey.PREF_LOC_PRECISION.toString(),"1");

        while(mReadings.size() < Integer.parseInt(precision)) {
            toaster("Launching scan " + (mReadings.size()+1) + " of " + precision);
            ((WifiManager) getSystemService(Context.WIFI_SERVICE)).startScan();

            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Log.e("WifiService", "Error while waiting scan results: " + e.getMessage());
                }
            }
        }

        unregisterReceiver(wifiReceiver);

        String officeNumber = mLocalizationClassifier.updateLocation(mReadings);

        if(!mAccountController.getAccount().getmOffice().equals(officeNumber)){
            AutomationController automationController = new AutomationController(this);
            automationController.enterOffice(officeNumber);
        }

        // Update account office if using Syndesi
        if(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
            Account oldAccount = mAccountController.getAccount();
            oldAccount.setmOffice(officeNumber);
            mAccountController.saveAccount(oldAccount);
        }

        mReadings.clear();

        if(wakeLock.isHeld()){
            wakeLock.release();
        }
    }

    public void toaster(String message){
        final String toastMessage = message;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WifiService.this, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void toaster(String message, int duration)
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
