package tcslab.syndesiapp.controllers.automation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by blais on 23.02.2017.
 */
public class PowerController extends BroadcastReceiver {
    private Context mAppContext;

    public PowerController(Context appContext) {
        mAppContext = appContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        Log.d("Battery", "Current level: " + Float.toString(batteryPct) + ", charging: " + Boolean.toString(isCharging));
        Toast.makeText(mAppContext, "Current level: " + Float.toString(batteryPct) + ", charging: " + Boolean.toString(isCharging), Toast.LENGTH_SHORT).show();
    }
}
