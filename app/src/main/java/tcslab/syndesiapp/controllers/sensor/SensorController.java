package tcslab.syndesiapp.controllers.sensor;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.account.AccountController;
import tcslab.syndesiapp.models.PreferenceKey;
import tcslab.syndesiapp.views.MainActivity;

import java.util.ArrayList;

/**
 * Manages sensors and reacts to settings changes to adapt the sensors in a singleton controller.
 * Created by Blaise on 01.05.2015.
 */
public class SensorController implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static SensorController mInstance;
    private Activity mActivity;
    private ArrayList<PendingIntent> mSensorsLauncher;
    private AlarmManager mAlarmManager;
    private ArrayList<String> mAvailableSensors;

    private SensorController(Activity activity) {
        this.mActivity = activity;

        //Get all sensors
        this.getSensorLaunchers();

        //Get the alarm manager
        mAlarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
            this.enableSensors();
        } else {
            this.disableSensors();
        }

        //Update the UI
        this.updateUI();
    }

    public static synchronized SensorController getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new SensorController(activity);
        }
        return mInstance;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceKey.PREF_SYNDESI_URL.toString())) {
            Log.d("PREF", "Server changed");
            final TextView connection = (TextView) mActivity.findViewById(R.id.server_display_status);
            String server_url = PreferenceManager.getDefaultSharedPreferences(mActivity).getString(PreferenceKey.PREF_SYNDESI_URL.toString(), "");

            if (server_url.equals("")) {
                connection.setText(R.string.connection_no_server_set);
            } else {
                AccountController.getInstance(mActivity).updateAccount();
                Log.d("PREF", "User account updated");
            }
        }
        if (key.equals(PreferenceKey.PREF_SENSOR_RATE.toString())) {
            if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
                this.disableSensors();
                this.enableSensors();
                Log.d("PREF", "Sensor polling rate changed");
            }
        }
        if (key.equals(PreferenceKey.PREF_SENSOR_PERM.toString())) {
            if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
                this.enableSensors();
                Log.d("PREF", "Sensors enabled");
            } else {
                this.disableSensors();
                Log.d("PREF", "Sensors disabled");
            }
        }
    }

    private void updateUI(){
        Log.d("Sensor", "Update UI");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
            ((TextView) mActivity.findViewById(R.id.sensors_status)).setText("");
        } else {
            ((TextView) mActivity.findViewById(R.id.sensors_status)).setText(R.string.sensors_disabled);
            ((TextView) mActivity.findViewById(R.id.server_display_status)).setText(R.string.connection_no_data);
        }

        String urlType;
        if(sharedPreferences.getString(PreferenceKey.PREF_SERVER_TYPE.toString(), "").equals("syndesi")){
            urlType = PreferenceKey.PREF_SYNDESI_URL.toString();
        }else{
            urlType = PreferenceKey.PREF_SENGEN_URL.toString();
        }

        if (sharedPreferences.getString(urlType, "").equals("")) {
            ((TextView) mActivity.findViewById(R.id.server_display_status)).setText(R.string.connection_no_server_set);
        }
    }

    private void enableSensors() {
        ((TextView) mActivity.findViewById(R.id.sensors_status)).setText("");
        //Set Alarm to launch the listener
        for(PendingIntent sensorLauncher : mSensorsLauncher){
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mActivity).getString(PreferenceKey.PREF_SENSOR_RATE.toString(), "60")) * 1000, sensorLauncher);
        }
    }

    private void disableSensors() {
        ((TextView) mActivity.findViewById(R.id.sensors_status)).setText(R.string.sensors_disabled);
        ((TextView) mActivity.findViewById(R.id.server_display_status)).setText(R.string.connection_no_data);
        ((MainActivity)mActivity).removeSensors();
        //Disable alarms
        for(PendingIntent sensorLauncher : mSensorsLauncher){
            mAlarmManager.cancel(sensorLauncher);
        }
    }

    /**
     * Get all the sensors available and build the PendingIntent to launch SensorService
     */
    private void getSensorLaunchers(){
        mSensorsLauncher = new ArrayList<PendingIntent>();
        SensorManager sensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mAvailableSensors = new ArrayList<>();
        //Get all the sensors listed in SensorList that are available on the device
        for(Integer sensorType : SensorList.sensorUsed){
            if (sensorManager.getDefaultSensor(sensorType) != null){
                //Build an Intent to launch the SensorService
                Intent sensorIntent = new Intent(mActivity, SensorService.class);
                sensorIntent.setAction(String.valueOf(sensorType));
                mSensorsLauncher.add(PendingIntent.getService(mActivity, 0, sensorIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                mAvailableSensors.add(SensorList.getStringType(sensorType));
            }
        }
    }

    public ArrayList<String> getmAvailableSensors() {
        return mAvailableSensors;
    }

    public void setmActivity(Activity activity) {
        this.mActivity = activity;
        this.updateUI();
    }
}
