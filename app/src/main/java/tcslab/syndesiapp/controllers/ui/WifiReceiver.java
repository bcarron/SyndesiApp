package tcslab.syndesiapp.controllers.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.models.PreferenceKey;
import tcslab.syndesiapp.views.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blais on 30.11.2016.
 */
public class WifiReceiver extends BroadcastReceiver {
    private MainActivity mActivity;
    private List<List<ScanResult>> mReadings = new ArrayList<>();

    public WifiReceiver(MainActivity activity) {
        mActivity = activity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WifiReceiver", intent.getAction());
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            mReadings.add(((WifiManager) mActivity.getSystemService(mActivity.WIFI_SERVICE)).getScanResults());

            String precision = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext()).getString(PreferenceKey.PREF_LOC_PRECISION.toString(),"1");
            if(mReadings.size() < Integer.parseInt(precision)){
                Toast.makeText(mActivity, "Scan " + mReadings.size() + " of " + precision, Toast.LENGTH_SHORT).show();
                ((WifiManager) mActivity.getSystemService(mActivity.WIFI_SERVICE)).startScan();
            }else {
                String officeNumber = mActivity.getmLocalizationController().updateLocation(mReadings);

                //Update the UI office status
                TextView officeTextView = (TextView) mActivity.findViewById(R.id.office_display);
                String newOfficeText;
                if (officeNumber.equals("-1.0")) {
                    newOfficeText = "Cannot locate you (missing training file?)";
                } else {
                    newOfficeText = mActivity.getString(R.string.office_display) + " " + officeNumber;
                }
                officeTextView.setText(newOfficeText);

                mReadings.clear();
            }
        }
    }
}
