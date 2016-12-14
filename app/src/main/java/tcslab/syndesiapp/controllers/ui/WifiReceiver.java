package tcslab.syndesiapp.controllers.ui;

import android.app.Service;
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
import tcslab.syndesiapp.controllers.localization.LocalizationController;
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
    private Context mAppContext;
    private LocalizationController mLocalizationController;
    private AccountController mAccountController;
    private List<List<ScanResult>> mReadings = new ArrayList<>();

    public WifiReceiver(Context appContext, LocalizationController localizationController) {
        Log.d("WifiReceiver", "New receiver");
        this.mAppContext = appContext;
        this.mAccountController = AccountController.getInstance(this.mAppContext);
        this.mLocalizationController = localizationController;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WifiReceiver", intent.getAction());
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            mReadings.add(((WifiManager) mAppContext.getSystemService(Service.WIFI_SERVICE)).getScanResults());

            String precision = PreferenceManager.getDefaultSharedPreferences(mAppContext.getApplicationContext()).getString(PreferenceKey.PREF_LOC_PRECISION.toString(),"1");
            if(mReadings.size() < Integer.parseInt(precision)){
                Toast.makeText(mAppContext, "Scan " + mReadings.size() + " of " + precision, Toast.LENGTH_SHORT).show();
                ((WifiManager) mAppContext.getSystemService(Service.WIFI_SERVICE)).startScan();
            }else {
                Log.d("WifiReceiver", "Starting loc");
                Toast.makeText(mAppContext, "Scan " + mReadings.size() + " of " + precision, Toast.LENGTH_SHORT).show();
                String officeNumber = mLocalizationController.updateLocation(mReadings);

                //Update the UI office status TODO IN LOCCONTROLLER AND UI RECEIVER
                /*TextView officeTextView = (TextView) mActivity.findViewById(R.id.loc_display);
                String newOfficeText;
                if (officeNumber != null) {
                    newOfficeText = mActivity.getString(R.string.loc_display) + ": " + officeNumber;
                } else {
                    newOfficeText = "Cannot locate you (missing training file?)";
                }
                officeTextView.setText(newOfficeText);*/

                // Update account office if using Syndesi
                if(PreferenceManager.getDefaultSharedPreferences(this.mAppContext).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
                    Account oldAccount = mAccountController.getAccount();
                    oldAccount.setmOffice(officeNumber);
                    mAccountController.saveAccount(oldAccount);
                }

                mReadings.clear();
            }
        }
    }
}
