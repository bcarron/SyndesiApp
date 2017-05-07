package tcslab.syndesiapp.controllers.localization;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import tcslab.syndesiapp.controllers.sensor.StepListener;
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
    private SensorManager mSensorManager;
    private StepListener mStepListener;
    private PendingIntent mLocalizationLauncher;
    private SharedPreferences mSharedPreferences;
    private double mIntervalModifier;
    private boolean mAlarmIsSet;
    private boolean mStopLocalization;
    private boolean mAutoLoc = false;


    private LocalizationController(Context appContext) {
        this.mAppContext = appContext;
        mAlarmManager = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);
        mSensorManager = (SensorManager) mAppContext.getSystemService(Context.SENSOR_SERVICE);
        mStepListener = new StepListener(mAppContext);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);

        // Set up default interval modifier
        mIntervalModifier = 1;

        // Default values at startup
        mAlarmIsSet = false;
        mStopLocalization = false;

        if (mSharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false)) {
            enableLocalization();
            if(mSharedPreferences.getBoolean(PreferenceKey.PREF_AUTO_LOC_PERM.toString(), false)){
                enableAutoLoc();
            }
        }else{
            disableLocalization();
            disableAutoLoc();
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
                enableLocalization();
            } else {
                disableLocalization();
            }
        } else if (key.equals(PreferenceKey.PREF_LOC_RATE.toString())) {
            if (sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false)) {
                disableLocalization();
                enableLocalization();
            }
        } else if (key.equals(PreferenceKey.PREF_AUTO_LOC_PERM.toString())) {
            if (sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false) && sharedPreferences.getBoolean(PreferenceKey.PREF_AUTO_LOC_PERM.toString(), false)) {
                enableAutoLoc();
            } else {
                disableAutoLoc();
            }
        }

        updateUI();
    }

    private void enableAutoLoc(){
        if (Build.VERSION.SDK_INT >= 19) {
            if(!mAutoLoc) {
                mSensorManager.registerListener(mStepListener, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);
                mAutoLoc = true;
            }
        }
    }

    private void disableAutoLoc(){
        if(mAutoLoc){
            mSensorManager.unregisterListener(mStepListener);
            mAutoLoc = false;
        }
    }

    private void enableLocalization(){
        // Launch Service for the localization
        if(!mAlarmIsSet && !mStopLocalization) {
            Intent localizationIntent = new Intent(mAppContext, WifiService.class);
            mLocalizationLauncher = PendingIntent.getService(mAppContext, 0, localizationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            int baseInterval = Integer.parseInt(mSharedPreferences.getString(PreferenceKey.PREF_LOC_RATE.toString(), "300"));
            int interval = (int) (baseInterval * mIntervalModifier * 1000);
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, mLocalizationLauncher);
            mAppContext.startService(localizationIntent);

            mAlarmIsSet = true;

            Log.d("PREF", "Localization enabled");
        }
    }


    private void disableLocalization(){
        // Stop the service for localization
        if(mLocalizationLauncher != null){
            mAlarmManager.cancel(mLocalizationLauncher);
        }

        mAlarmIsSet = false;

        Log.d("PREF", "Localization disabled");
    }

    public void startLocalization(){
        mStopLocalization = false;

        if (mSharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false)) {
            enableLocalization();
        }
    }

    public void stopLocalization(){
        // Prevent localization to be enabled by change in preferences
        mStopLocalization = true;
        disableLocalization();
    }

    private void updateUI(){
        Boolean status;

        status = mSharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false);

        // Send broadcast to update the UI
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_LOC_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_LOC_STATUS.toString(), status);
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(localIntent);
    }

    public void setmAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
        updateUI();
    }

    public void setmIntervalModifier(Double mIntervalModifier) {
        if(mIntervalModifier != this.mIntervalModifier || this.mStopLocalization){
            this.mIntervalModifier = mIntervalModifier;
            stopLocalization();
            startLocalization();
        }
    }
}
