package tcslab.syndesiapp.controllers.sensor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import tcslab.syndesiapp.controllers.localization.WifiService;
import tcslab.syndesiapp.models.PreferenceKey;

import java.util.Date;

/**
 * Created by blaise on 05.05.17.
 */
public class StepListener implements SensorEventListener {
    private Context mAppContext;
    private Date mLastScan;

    public StepListener(Context appContext){
        this.mAppContext = appContext;
        mLastScan = new Date();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);

        if (sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false) && sharedPreferences.getBoolean(PreferenceKey.PREF_AUTO_LOC_PERM.toString(), false)) {
            if (mLastScan.before(new Date(System.currentTimeMillis() - 10 * 1000))) {
                Intent localizationIntent = new Intent(mAppContext, WifiService.class);
                mAppContext.startService(localizationIntent);
                mLastScan = new Date();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
