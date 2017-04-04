package tcslab.syndesiapp.controllers.power;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import tcslab.syndesiapp.controllers.localization.LocalizationController;
import tcslab.syndesiapp.controllers.sensor.SensorController;

/**
 * Created by blaise on 23.02.2017.
 */
public class PowerController extends IntentService {
    public SensorController sensorController;
    public LocalizationController localizationController;

    public PowerController() {
        super("PowerController");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorController = SensorController.getInstance(this);
        localizationController = LocalizationController.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent batteryStatus = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        if (isCharging){
            // Set max sensing rate
            sensorController.setmIntervalModifier(1.0);
            sensorController.startSensing();
        }else{
            if (batteryPct > 0.5){
                // Set max sensing rate
                sensorController.setmIntervalModifier(1.0);
                sensorController.startSensing();
            }else if (batteryPct > 0.2){
                // Reduce sensing rate by half
                sensorController.setmIntervalModifier(2.0);
                sensorController.startSensing();
            }else{
                // Stop sensing
                sensorController.stopSensing();
            }
        }

        Log.d("Battery", "Current level: " + Float.toString(batteryPct) + ", charging: " + Boolean.toString(isCharging));
    }
}
