package tcslab.syndesiapp.controllers.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.views.MainActivity;

import java.util.List;

/**
 * Created by blais on 30.11.2016.
 */
public class WifiReceiver extends BroadcastReceiver {
    private MainActivity mActivity;

    public WifiReceiver(MainActivity activity) {
        mActivity = activity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WifiReceiver", intent.getAction());
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

            ScanResult resWifi;
            List<ScanResult> apsList = ((WifiManager) mActivity.getSystemService(mActivity.WIFI_SERVICE)).getScanResults();
            String officeNumber = mActivity.getmLocalizationController().updateLocation(apsList);

            //Update the UI office status
            TextView officeTextView = (TextView) mActivity.findViewById(R.id.office_display);
            String newOfficeText = mActivity.getString(R.string.office_display) + " " + officeNumber;
            officeTextView.setText(newOfficeText);
        }
    }
}
