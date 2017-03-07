package tcslab.syndesiapp.controllers.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.sensor.SensorController;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;
import tcslab.syndesiapp.models.SensorData;
import tcslab.syndesiapp.views.MainActivity;

/**
 * Updates the user interface by receiving local broadcasts from controllers and services.
 *
 * Created by Blaise on 01.05.2015.
 */
public class UIReceiver extends BroadcastReceiver {
    private Activity mActivity;
    private SensorController mSensorController;
    private SharedPreferences mPreferences;


    public UIReceiver(Activity activity) {
        mActivity = activity;
        mSensorController = SensorController.getInstance(mActivity);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BroadcastType.BCAST_TYPE_SERVER_STATUS.toString())) {
            //Update the UI server status
            String response = intent.getStringExtra(BroadcastType.BCAST_EXTRA_SERVER_RESPONSE.toString());
            TextView server = (TextView) mActivity.findViewById(R.id.server_display_status);
            server.setText(response);
        }else if (intent.getAction().equals(BroadcastType.BCAST_TYPE_CONTROLLER_STATUS.toString())) {
            //Update the UI controller status
            String response = intent.getStringExtra(BroadcastType.BCAST_EXTRA_SERVER_RESPONSE.toString());
            TextView server = (TextView) mActivity.findViewById(R.id.controller_display_status);
            server.setText(response);
        }else if (intent.getAction().equals(BroadcastType.BCAST_TYPE_SENSOR_STATUS.toString())) {
            //Update the UI sensor status
            String status = intent.getStringExtra(BroadcastType.BCAST_EXTRA_SENSOR_STATUS.toString());
            TextView sensor = (TextView) mActivity.findViewById(R.id.sensors_status);
            sensor.setText(status);
            if(status.equals(mActivity.getString(R.string.sensors_disabled))){
                ((MainActivity) mActivity).removeSensors();
            }
        } else if(intent.getAction().equals(BroadcastType.BCAST_TYPE_LOC_STATUS.toString())){
            Boolean status = intent.getBooleanExtra(BroadcastType.BCAST_EXTRA_LOC_STATUS.toString(), false);
            String office = mPreferences.getString(PreferenceKey.PREF_CURRENT_POSITION.toString(), null);
            TextView newOfficeText = (TextView) mActivity.findViewById(R.id.loc_display);
            Button relocateBtn = (Button) mActivity.findViewById(R.id.btnRelocate);
            if(status) {
                if(office != null) {
                    newOfficeText.setText(mActivity.getString(R.string.loc_display) + " " + office);
                }else{
                    newOfficeText.setText(mActivity.getString(R.string.loc_scanning));
                }
                relocateBtn.setVisibility(View.VISIBLE);
            }else{
                newOfficeText.setText(mActivity.getString(R.string.loc_disabled));
                relocateBtn.setVisibility(View.INVISIBLE);
            }
        } else if(intent.getAction().equals(BroadcastType.BCAST_TYPE_LOC_POSITION.toString())){
            String office = intent.getStringExtra(BroadcastType.BCAST_EXTRA_LOC_OFFICE.toString());
            TextView newOfficeText = (TextView) mActivity.findViewById(R.id.loc_display);
            if(office != null) {
                newOfficeText.setText(mActivity.getString(R.string.loc_display) + " " + office);
                mPreferences.edit().putString(PreferenceKey.PREF_CURRENT_POSITION.toString(), office).apply();
            }else{
                newOfficeText.setText(mActivity.getString(R.string.loc_scanning));
            }
        } else {
            Float data = intent.getFloatExtra(BroadcastType.BCAST_EXTRA_SENSOR_DATA.toString(), 0);
            SensorData sensorData = new SensorData("", data, intent.getAction());

            //Add sensor reading to the UI
            ((MainActivity)mActivity).addSensor(sensorData);
        }
    }
}
