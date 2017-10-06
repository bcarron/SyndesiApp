package tcslab.syndesiapp.controllers.localization;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import tcslab.syndesiapp.controllers.account.AccountController;
import tcslab.syndesiapp.controllers.automation.DemoAutomationController;
import tcslab.syndesiapp.models.Account;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Perform the WiFi access point scans in order to use the localization classifier.
 *
 * Created by blais on 30.11.2016.
 */
public class WifiService extends IntentService implements WifiCallback {
    private LocalizationClassifier mLocalizationClassifier;
    private AccountController mAccountController;
    private DemoAutomationController mAutomationController;
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
        resetUI();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putBoolean(PreferenceKey.PREF_LOC_IN_PROGRESS.toString(), true).apply();

        // TODO: Check if the user moved otherwise no need to perform localization

        mLocalizationClassifier = LocalizationClassifier.getInstance(this);
        mAccountController = AccountController.getInstance(this);
        mAutomationController = DemoAutomationController.getInstance(this.getApplicationContext());

        // Register the Wifi receiver
        // Register the Broadcast listener
        WifiReceiver wifiReceiver = new WifiReceiver(this, this);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Get the number of scans from the preferences
        String precision = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKey.PREF_LOC_PRECISION.toString(),"1");

        // Perform the scans
        toaster("Starting WiFi scan", Toast.LENGTH_SHORT);
        while(mReadings.size() < Integer.parseInt(precision)) {
//            toaster("Launching scan " + (mReadings.size()+1) + " of " + precision);
            ((WifiManager) getApplicationContext().getSystemService(Service.WIFI_SERVICE)).startScan();

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

        String newPosition = mLocalizationClassifier.updateLocation(mReadings);

        // Update position for the automation

        mAutomationController.updatePosition(newPosition);

        // Update account office if using Syndesi
        if(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
            Account oldAccount = mAccountController.getAccount();
            oldAccount.setmOffice(newPosition);
            mAccountController.saveAccount(oldAccount);
        }

        // Clear readings for next localization
        mReadings.clear();
        sharedPrefEditor.putBoolean(PreferenceKey.PREF_LOC_IN_PROGRESS.toString(), false).apply();

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

        Boolean permission = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKey.PREF_PERMISSION.toString(), false);

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

    private void resetUI(){
        // Send broadcast to update the UI
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_LOC_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_LOC_STATUS.toString(), true);
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_LOC_RESET.toString(), true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
