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
import tcslab.syndesiapp.controllers.localization.LocalizationController;
import tcslab.syndesiapp.controllers.localization.WifiService;
import tcslab.syndesiapp.controllers.automation.AutomationAdapter;
import tcslab.syndesiapp.controllers.ui.UIReceiver;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.NodeType;
import tcslab.syndesiapp.models.PreferenceKey;

import java.util.ArrayList;
import java.util.Date;

/**
 * Controls the environment.
 *
 * Created by Blaise on 04.05.2017.
 */
public class EnvironmentControlActivity extends AppCompatActivity {
    private UIReceiver uiReceiver;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor msharedPrefEditor;
    private AutomationAdapter mAutomationAdapter;
    private LocalizationController mLocalizationController;
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
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        msharedPrefEditor = mSharedPreferences.edit();

        // Set stored preferences
        Float targetLight = mSharedPreferences.getFloat(PreferenceKey.PREF_TARGET_LIGHT.toString(), 250);
        Float targetTemp = mSharedPreferences.getFloat(PreferenceKey.PREF_TARGET_TEMP.toString(), 22);
        ((EditText) findViewById(R.id.light_target)).setText(targetLight.toString());
        ((EditText) findViewById(R.id.temp_target)).setText(targetTemp.toString());

        //Creates the broadcast receiver that updates the UI
        uiReceiver = new UIReceiver(this);
        mHandler = new Handler();
        mRefreshUI = new RefreshMessages();
        mRefreshDelay = 5 * 1000;

        // Set the localization controller
        mLocalizationController = LocalizationController.getInstance(this);

        //Set the sensor list
        ListView listView = (ListView) findViewById(R.id.automation_list);
        mMessages = new ArrayList<>();
        mAutomationAdapter = new AutomationAdapter(this, mMessages);
        listView.setAdapter(mAutomationAdapter);

        // Updates the location
//        relocate(new View(this));

        if (!mSharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
            addMessage(new AutomationStatus("", this.getString(R.string.sensors_disabled), NodeType.alarm, "on", new Date(System.currentTimeMillis() * 2)));
        }

        if (!mSharedPreferences.getBoolean(PreferenceKey.PREF_AUT_PERM.toString(), false)) {
            addMessage(new AutomationStatus("", this.getString(R.string.automation_disabled), NodeType.alarm, "on", new Date(System.currentTimeMillis() * 2)));
        }
    }

    //Life cycle management
    @Override
    protected void onResume() {
        super.onResume();

        //Reset the context on the controllers
        mLocalizationController.setmAppContext(this);
        mLocalizationController.startLocalization();

        //Register the Broadcast listener
        IntentFilter filter = new IntentFilter(BroadcastType.BCAST_TYPE_AUT_STATUS.toString());
        LocalBroadcastManager.getInstance(this).registerReceiver(uiReceiver, filter);

        // Handler to refresh messages
        mHandler.postDelayed(mRefreshUI, mRefreshDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mLocalizationController.stopLocalization();

        //Unregister the Broadcast listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiReceiver);

        // Stop Handler
        mHandler.removeCallbacks(mRefreshUI);
    }

    private void checkMessages(){
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
        if (mSharedPreferences.getBoolean(PreferenceKey.PREF_LOC_PERM.toString(), false)) {
//            Toast.makeText(this, "Starting WiFi scan", Toast.LENGTH_SHORT).show();
            startService(new Intent(this, WifiService.class));
        } else {
            addMessage(new AutomationStatus("", this.getString(R.string.loc_disabled), NodeType.alarm, "on", new Date(System.currentTimeMillis() * 2)));
        }
    }

    public void savePreferences(View v){
        EditText lightText = (EditText) findViewById(R.id.light_target);
        EditText tempText = (EditText) findViewById(R.id.temp_target);

        String targetLight = lightText.getText().toString();
        String targetTemp = tempText.getText().toString();

        if(!targetLight.equals("") && !targetTemp.equals("")){
            msharedPrefEditor.putFloat(PreferenceKey.PREF_TARGET_LIGHT.toString(), Float.parseFloat(targetLight)).apply();
            msharedPrefEditor.putFloat(PreferenceKey.PREF_TARGET_TEMP.toString(), Float.parseFloat(targetTemp)).apply();
            Toast.makeText(this, R.string.preferences_saved, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, R.string.preferences_empty_fields, Toast.LENGTH_LONG).show();
        }
    }

    private class RefreshMessages implements Runnable{
        @Override
        public void run(){
            checkMessages();
            mHandler.postDelayed(this, mRefreshDelay);
        }
    }
}
