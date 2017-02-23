package tcslab.syndesiapp.controllers.power;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by blaise on 23.02.2017.
 */
public class PowerService extends IntentService {

    public PowerService() {
        super("PowerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent batteryStatus = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        Log.d("Battery", "Current level: " + Float.toString(batteryPct) + ", charging: " + Boolean.toString(isCharging));
    }
}
