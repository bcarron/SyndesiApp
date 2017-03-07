package tcslab.syndesiapp.controllers.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import tcslab.syndesiapp.controllers.network.SendDataTask;
import tcslab.syndesiapp.models.PreferenceKey;

/**
 * Listens to sensors and prepares an AsyncTask to send data to the server and update the user interface.
 *
 * Created by Blaise on 27.05.2015.
 */
public class SensorListener implements SensorEventListener {
    private Context mAppContext;
    private SensorManager mSensorManager;
    private PowerManager.WakeLock mWakeLock;

    public SensorListener(Context appContext, SensorManager sensorManager){
        this.mAppContext = appContext;
        this.mSensorManager = sensorManager;
        mWakeLock = ((PowerManager) appContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
        mWakeLock.acquire();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(PreferenceManager.getDefaultSharedPreferences(mAppContext).getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)){
            AsyncTask sendData = new SendDataTask(mAppContext);
            sendData.execute(new SensorEvent[]{sensorEvent});
        }

        mSensorManager.unregisterListener(this);
        mWakeLock.release();
    }
}
