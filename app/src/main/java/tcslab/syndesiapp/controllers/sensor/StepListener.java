package tcslab.syndesiapp.controllers.sensor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.preference.PreferenceManager;
import tcslab.syndesiapp.controllers.localization.WifiService;
import tcslab.syndesiapp.models.PreferenceKey;

/**
 * Listens to the steps counter and trigger a new localization task if the number of steps has changed
 *
 * Created by Blaise on 05.05.17.
 */
public class StepListener implements SensorEventListener {
    private Context mAppContext;

    public StepListener(Context appContext){
        this.mAppContext = appContext;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);

        if (sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false) && sharedPreferences.getBoolean(PreferenceKey.PREF_AUTO_LOC_PERM.toString(), false)) {
            if (!sharedPreferences.getBoolean(PreferenceKey.PREF_LOC_IN_PROGRESS.toString(), false)) {
                Intent localizationIntent = new Intent(mAppContext, WifiService.class);
                mAppContext.startService(localizationIntent);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
