package tcslab.syndesiapp.controllers.power;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by blaise on 23.02.2017.
 */
public class PowerController extends IntentService {

    public PowerController() {
        super("PowerController");
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
            // TODO: Set max polling rate
        }else{
            if (batteryPct > 0.6){
                // TODO: Set max polling rate
            }else if (batteryPct > 0.2){
                // TODO: Set reduced polling rate
            }else{
                // TODO: Disable all sensors
            }
        }

        Log.d("Battery", "Current level: " + Float.toString(batteryPct) + ", charging: " + Boolean.toString(isCharging));
    }
}
