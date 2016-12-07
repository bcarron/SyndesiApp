package tcslab.syndesiapp.controllers.localization;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by blais on 30.11.2016.
 */
public class WifiService extends IntentService {
    public WifiService() {
        super("WifiListener");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ((WifiManager) getSystemService(this.WIFI_SERVICE)).startScan();
    }
}
