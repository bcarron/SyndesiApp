package tcslab.syndesiapp.controllers.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.account.AccountController;
import tcslab.syndesiapp.models.Account;
import tcslab.syndesiapp.models.BroadcastType;
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
    private AccountController mAccountController;
    private Boolean visible = true;

    public WifiReceiver(MainActivity activity) {
        Log.d("WifiReceiver", "New receiver");
        this.mActivity = activity;
        this.mAccountController = AccountController.getInstance(this.mActivity);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WifiReceiver", intent.getAction());
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && visible) {
            mReadings.add(((WifiManager) mActivity.getSystemService(mActivity.WIFI_SERVICE)).getScanResults());

            String precision = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext()).getString(PreferenceKey.PREF_LOC_PRECISION.toString(),"1");
            if(mReadings.size() < Integer.parseInt(precision)){
                Toast.makeText(mActivity, "Scan " + mReadings.size() + " of " + precision, Toast.LENGTH_SHORT).show();
                ((WifiManager) mActivity.getSystemService(mActivity.WIFI_SERVICE)).startScan();
            }else {
                Log.d("WifiReceiver", "Starting loc");
                Toast.makeText(mActivity, "Scan " + mReadings.size() + " of " + precision, Toast.LENGTH_SHORT).show();
                String officeNumber = mActivity.getmLocalizationController().updateLocation(mReadings);

                //Update the UI office status
                TextView officeTextView = (TextView) mActivity.findViewById(R.id.loc_display);
                String newOfficeText;
                if (officeNumber != null) {
                    newOfficeText = mActivity.getString(R.string.loc_display) + ": " + officeNumber;
                } else {
                    newOfficeText = "Cannot locate you (missing training file?)";
                }
                officeTextView.setText(newOfficeText);

                // Update account office if using Syndesi
                if(PreferenceManager.getDefaultSharedPreferences(this.mActivity).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
                    Account oldAccount = mAccountController.getAccount();
                    oldAccount.setmOffice(officeNumber);
                    mAccountController.saveAccount(oldAccount);
                }

                mReadings.clear();
            }
        }else if(intent.getAction().equals(BroadcastType.BCAST_TYPE_LOC_STATUS.toString())){
            Boolean status = intent.getBooleanExtra(BroadcastType.BCAST_EXTRA_LOC_STATUS.toString(), false);
            String office = intent.getStringExtra(BroadcastType.BCAST_EXTRA_LOC_OFFICE.toString());
            TextView newOfficeText = (TextView) mActivity.findViewById(R.id.loc_display);
            Button relocateBtn = (Button) mActivity.findViewById(R.id.btnRelocate);
            if(status) {
                if(office != null) {
                    newOfficeText.setText(mActivity.getString(R.string.loc_display) + " " + office);
                }else{
                    newOfficeText.setText(mActivity.getString(R.string.loc_scanning));
                }
                relocateBtn.setVisibility(View.VISIBLE);
                this.visible = true;
            }else{
                newOfficeText.setText(mActivity.getString(R.string.loc_disabled));
                relocateBtn.setVisibility(View.INVISIBLE);
                this.visible = false;
            }
        }
    }
}
