package tcslab.syndesiapp.controllers.localization;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;

/**
 * Handle the localization preference in the settings and enable or disable the associated localization service.
 *
 * Created by Blaise on 30.11.2016.
 */

public class LocalizationController implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static LocalizationController mInstance;
    private Context mAppContext;
    private AlarmManager mAlarmManager;
    private PendingIntent mLocalizationLauncher;
    private long mLocalizationInterval = 300000;


    private LocalizationController(Context appContext) {
        this.mAppContext = appContext;
        mAlarmManager = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        if (sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false)) {
            startLocalization();
        }else{
            stopLocalization();
        }
    }

    public static synchronized LocalizationController getInstance(Context appContext) {
        if (mInstance == null) {
            mInstance = new LocalizationController(appContext.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceKey.PREF_LOC_PERM.toString())) {
            if (sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false)) {
                startLocalization();
            } else {
                stopLocalization();
            }
        }
        updateUI();
    }

    private void startLocalization(){
        // Launch Service for the localization
        Intent localizationIntent = new Intent(mAppContext, WifiService.class);
        mLocalizationLauncher = PendingIntent.getService(mAppContext, 0, localizationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + mLocalizationInterval, mLocalizationInterval, mLocalizationLauncher);
        mAppContext.startService(localizationIntent);

        Log.d("PREF", "Localization enabled");
    }


    private void stopLocalization(){
        // Stop the service for localization
        if(mLocalizationLauncher != null){
            mAlarmManager.cancel(mLocalizationLauncher);
        }

        Log.d("PREF", "Localization disabled");
    }

    private void updateUI(){
        Boolean status;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);

        status = sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false);

        // Send broadcast to update the UI
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_LOC_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_LOC_STATUS.toString(), status);
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(localIntent);
    }

    public void setmAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
        updateUI();
    }
}
