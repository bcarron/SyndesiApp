package tcslab.syndesiapp.controllers.sensor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.wifi.WifiManager;
import android.util.Log;
import tcslab.syndesiapp.controllers.localization.WifiService;

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
        if(mLastScan.before(new Date(System.currentTimeMillis() - 10 * 1000))){
            Intent localizationIntent = new Intent(mAppContext, WifiService.class);
            mAppContext.startService(localizationIntent);
            mLastScan = new Date();
            Log.d("Steps", String.valueOf(sensorEvent.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
