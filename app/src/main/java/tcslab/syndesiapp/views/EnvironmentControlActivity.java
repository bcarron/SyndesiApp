package tcslab.syndesiapp.views;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.*;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.automation.AutomationStatus;
import tcslab.syndesiapp.controllers.localization.WifiService;
import tcslab.syndesiapp.controllers.automation.AutomationAdapter;
import tcslab.syndesiapp.controllers.ui.UIReceiver;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;

import java.util.ArrayList;
import java.util.Date;

/**
 * Controls the nodes connected to the system.
 *
 * Created by Blaise on 31.05.2015.
 */
public class EnvironmentControlActivity extends AppCompatActivity {
    private UIReceiver uiReceiver;
    private SharedPreferences.Editor mAccountPrefEditor;
    private AutomationAdapter mAutomationAdapter;
    private ArrayList<AutomationStatus> mMessages;
    private Handler mHandler;
    private Runnable mRefreshUI;
    private int mRefreshDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the layout
        setContentView(R.layout.environment_control);

        //Set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Account preferences
        SharedPreferences accountPref = PreferenceManager.getDefaultSharedPreferences(this);
        mAccountPrefEditor = accountPref.edit();

        // Set stored preferences
        Float targetLight = accountPref.getFloat(PreferenceKey.PREF_TARGET_LIGHT.toString(), 250);
        Float targetTemp = accountPref.getFloat(PreferenceKey.PREF_TARGET_TEMP.toString(), 22);
        ((EditText) findViewById(R.id.light_target)).setText(targetLight.toString());
        ((EditText) findViewById(R.id.temp_target)).setText(targetTemp.toString());

        //Creates the broadcast receiver that updates the UI
        uiReceiver = new UIReceiver(this);
        mHandler = new Handler();
        mRefreshUI = new RefreshMessages();
        mRefreshDelay = 5 * 1000;

        //Set the sensor list
        ListView listView = (ListView) findViewById(R.id.automation_list);
        mMessages = new ArrayList<>();
        mAutomationAdapter = new AutomationAdapter(this, mMessages);
        listView.setAdapter(mAutomationAdapter);

        // Updates the location
        relocate(new View(this));
    }

    //Life cycle management
    @Override
    protected void onResume() {
        super.onResume();

        //Register the Broadcast listener
        IntentFilter filter = new IntentFilter(BroadcastType.BCAST_TYPE_AUT_STATUS.toString());
        LocalBroadcastManager.getInstance(this).registerReceiver(uiReceiver, filter);

        // Handler to refresh messages
        mHandler.postDelayed(mRefreshUI, mRefreshDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Unregister the Broadcast listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiReceiver);

        // Stop Handler
        mHandler.removeCallbacks(mRefreshUI);
    }

    public void checkMessages(){
        for(int i = 0; i < mMessages.size(); i++) {
            AutomationStatus currentStatus = mMessages.get(i);
            if(currentStatus.getmTimestamp().before(new Date(System.currentTimeMillis() - 30 * 1000))){
                mMessages.remove(i);
            }
        }

        mAutomationAdapter.notifyDataSetChanged();
    }

    public void addMessage(AutomationStatus status){
        Boolean sensorExist = false;

        for(AutomationStatus currentStatus: mMessages) {
            if (currentStatus.getmOffice().equals(status.getmOffice()) && currentStatus.getmMessage().equals(status.getmMessage())) {
                currentStatus.setmTimestamp(new Date());
                sensorExist = true;
            }
        }

        if(!sensorExist){
            mMessages.add(status);
        }

        mAutomationAdapter.notifyDataSetChanged();
    }

    public void relocate(View v){
        Toast.makeText(this, "Starting WiFi scan", Toast.LENGTH_SHORT).show();
        startService(new Intent(this, WifiService.class));
    }

    public void savePreferences(View v){
        EditText lightText = (EditText) findViewById(R.id.light_target);
        EditText tempText = (EditText) findViewById(R.id.temp_target);

        String targetLight = lightText.getText().toString();
        String targetTemp = tempText.getText().toString();

        if(!targetLight.equals("") && !targetTemp.equals("")){
            mAccountPrefEditor.putFloat(PreferenceKey.PREF_TARGET_LIGHT.toString(), Float.parseFloat(targetLight)).apply();
            mAccountPrefEditor.putFloat(PreferenceKey.PREF_TARGET_TEMP.toString(), Float.parseFloat(targetTemp)).apply();
            Toast.makeText(this, R.string.preferences_saved, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, R.string.preferences_empty_fields, Toast.LENGTH_LONG).show();
        }
    }

    private class RefreshMessages implements Runnable{
        public void run(){
            checkMessages();
            mHandler.postDelayed(this, mRefreshDelay);
        }
    }
}
