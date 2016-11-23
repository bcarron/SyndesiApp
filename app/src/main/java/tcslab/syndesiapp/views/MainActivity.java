package tcslab.syndesiapp.views;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.account.AccountController;
import tcslab.syndesiapp.controllers.sensor.SensorAdapter;
import tcslab.syndesiapp.controllers.sensor.SensorController;
import tcslab.syndesiapp.controllers.sensor.SensorList;
import tcslab.syndesiapp.controllers.ui.UIReceiver;
import tcslab.syndesiapp.models.Account;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;
import tcslab.syndesiapp.models.SensorData;

import java.util.ArrayList;

/**
 * Displays the sensors readings and the server status.
 * Created by Blaise on 27.04.2015.
 */
public class MainActivity extends AppCompatActivity {
    private UIReceiver uiReceiver;
    private ArrayList<SensorData> mSensorsList;
    private SensorAdapter mSensorsAdapter;
    private AccountController mAccountController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set default settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //Set the layout
        setContentView(R.layout.activity_main);
        //Set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Set the sensor list
        ListView listView = (ListView) findViewById(R.id.sensor_list);
        mSensorsList = new ArrayList<>();
        mSensorsAdapter = new SensorAdapter(this, mSensorsList);
        listView.setAdapter(mSensorsAdapter);

        //Set the account controller to use with Syndesi server (legacy)
        this.mAccountController = AccountController.getInstance(getApplicationContext());
        Account account = mAccountController.getAccount();
        String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Account newAccount = new Account(id, "", "", "", 0, 0, SensorController.getInstance(this).getmAvailableSensors());
        if (account == null) {
            AccountController.getInstance(getApplicationContext()).createAccount(newAccount);
        }else{
            AccountController.getInstance(getApplicationContext()).saveAccount(newAccount);
        }

        //Creates the broadcast receiver that updates the UI
        uiReceiver = new UIReceiver(this);
        //Set the preferences listener
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(SensorController.getInstance(this));
    }

    public void removeSensors(){
        mSensorsList.clear();
        mSensorsAdapter.notifyDataSetChanged();
    }

    public void addSensor(SensorData sensor){
        Boolean sensorExist = false;
        for(SensorData currentSensor : mSensorsList) {
            if (currentSensor.getmDataType().equals(sensor.getmDataType())) {
                currentSensor.setmData(sensor.getmData());
                sensorExist = true;
            }
        }
        if(!sensorExist){
            mSensorsList.add(sensor);
        }
        mSensorsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu clicks
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }else if(id == R.id.action_controller){
            startActivity(new Intent(this, NodesControllerActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Life cycle management
    @Override
    protected void onResume() {
        super.onResume();
        //Reset the context on the sensor controller
        SensorController.getInstance(this).setmActivity(this);
        //Register the Broadcast listener
        IntentFilter filter = new IntentFilter();
        for(Integer sensorType : SensorList.sensorUsed){
            filter.addAction(String.valueOf(sensorType));
        }
        filter.addAction(String.valueOf(BroadcastType.BCAST_TYPE_SERVER_STATUS));
        filter.addAction(String.valueOf(BroadcastType.BCAST_TYPE_CONTROLLER_STATUS));
        LocalBroadcastManager.getInstance(this).registerReceiver(uiReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unregister the Broadcast listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(String.valueOf(R.id.sensors_status), ((TextView) findViewById(R.id.sensors_status)).getText().toString());
        outState.putString(String.valueOf(R.id.server_display_status), ((TextView) findViewById(R.id.server_display_status)).getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        ((TextView) findViewById(R.id.sensors_status)).setText(savedInstanceState.getString(String.valueOf(R.id.sensors_status)));
        ((TextView) findViewById(R.id.server_display_status)).setText(savedInstanceState.getString(String.valueOf(R.id.server_display_status)));
    }
}
