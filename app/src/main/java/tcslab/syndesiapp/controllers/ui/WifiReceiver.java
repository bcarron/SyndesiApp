package tcslab.syndesiapp.controllers.ui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import tcslab.syndesiapp.controllers.localization.WifiService;

/**
 * Created by blais on 30.11.2016.
 */
public class WifiReceiver extends BroadcastReceiver {
    private WifiService wifiService;

    public WifiReceiver(WifiService wifiService) {
        this.wifiService = wifiService;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WifiReceiver", intent.getAction());
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            wifiService.sendResults(((WifiManager) wifiService.getSystemService(Service.WIFI_SERVICE)).getScanResults());
        }
    }
}
