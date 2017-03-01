package tcslab.syndesiapp.controllers.sensor;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;
import tcslab.syndesiapp.models.SensorData;
import tcslab.syndesiapp.views.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages sensors and reacts to settings changes to adapt the sensors in a singleton controller.
 *
 * Created by Blaise on 01.05.2015.
 */
public class SensorController implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static SensorController mInstance;
    private Context mAppContext;
    private ArrayList<PendingIntent> mSensorsLauncher;
    private AlarmManager mAlarmManager;
    private ArrayList<String> mAvailableSensors;
    private HashMap<String, Float> mLastSensorValues;

    private SensorController(Context appContext) {
        this.mAppContext = appContext;

        //Get all sensors
        getSensorLaunchers();

        //Get the alarm manager
        mAlarmManager = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

        // Set up the list of last values
        mLastSensorValues = new HashMap<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
            enableSensors();
        } else {
            disableSensors();
        }

        //Update the UI
        updateUI();
    }

    public static synchronized SensorController getInstance(Context appContext) {
        if (mInstance == null) {
            mInstance = new SensorController(appContext);
        }
        return mInstance;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceKey.PREF_SENSOR_RATE.toString())) {
            if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
                disableSensors();
                enableSensors();
                Log.d("PREF", "Sensor polling rate changed");
            }
        }
        if (key.equals(PreferenceKey.PREF_SENSOR_PERM.toString())) {
            if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
                enableSensors();
                Log.d("PREF", "Sensors enabled");
            } else {
                disableSensors();
                Log.d("PREF", "Sensors disabled");
            }
        }
    }

    private void updateUI(){
        Log.d("Sensor", "Update UI");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        if (sharedPreferences.getBoolean(PreferenceKey.PREF_SENSOR_PERM.toString(), false)) {
            Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_SENSOR_STATUS.toString());
            localIntent.putExtra(BroadcastType.BCAST_EXTRA_SENSOR_STATUS.toString(), "");
            LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(localIntent);
        } else {
            Intent sensorIntent = new Intent(BroadcastType.BCAST_TYPE_SENSOR_STATUS.toString());
            sensorIntent.putExtra(BroadcastType.BCAST_EXTRA_SENSOR_STATUS.toString(), mAppContext.getString(R.string.sensors_disabled));
            LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(sensorIntent);

            Intent serverIntent = new Intent(BroadcastType.BCAST_TYPE_SERVER_STATUS.toString());
            serverIntent.putExtra(BroadcastType.BCAST_EXTRA_SERVER_RESPONSE.toString(), mAppContext.getString(R.string.connection_no_data));
            LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(serverIntent);
        }
    }

    private void enableSensors() {
        //Set Alarm to launch the listener
        for(PendingIntent sensorLauncher : mSensorsLauncher){
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mAppContext).getString(PreferenceKey.PREF_SENSOR_RATE.toString(), "60")) * 1000, sensorLauncher);
        }

        updateUI();
    }

    private void disableSensors() {
        ((MainActivity) mAppContext).removeSensors();
        //Disable alarms
        for(PendingIntent sensorLauncher : mSensorsLauncher){
            mAlarmManager.cancel(sensorLauncher);
        }
        updateUI();

    }


    /**
     * Get all the sensors available and build the PendingIntent to launch SensorService
     */
    private void getSensorLaunchers(){
        mSensorsLauncher = new ArrayList<PendingIntent>();
        SensorManager sensorManager = (SensorManager) mAppContext.getSystemService(Context.SENSOR_SERVICE);
        mAvailableSensors = new ArrayList<>();
        //Get all the sensors listed in SensorList that are available on the device
        for(Integer sensorType : SensorList.sensorUsed){
            if (sensorManager.getDefaultSensor(sensorType) != null){
                //Build an Intent to launch the SensorService
                Intent sensorIntent = new Intent(mAppContext, SensorService.class);
                sensorIntent.setAction(String.valueOf(sensorType));
                mSensorsLauncher.add(PendingIntent.getService(mAppContext, 0, sensorIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                mAvailableSensors.add(SensorList.getStringType(sensorType));
            }
        }
    }

    public void setLastSensorValue(SensorData sensor){
        String sensorType = SensorList.getStringType(Integer.parseInt(sensor.getmDataType()));
        Float data = sensor.getmData();

        mLastSensorValues.put(sensorType, data);
    }

    public Float getLastSensorValue(String sensorType){
        return mLastSensorValues.get(sensorType);
    }

    public ArrayList<String> getmAvailableSensors() {
        return mAvailableSensors;
    }

    public void setmAppContext(Activity activity) {
        this.mAppContext = activity;
        updateUI();
    }
}
