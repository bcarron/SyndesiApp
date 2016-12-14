package tcslab.syndesiapp.controllers.network;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import tcslab.syndesiapp.controllers.sensor.SensorList;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;

/**
 * Sends data to the server and fire broadcast intents to update the user interface
 *
 * Created by Blaise on 30.04.2015.
 */
public class SendDataTask extends AsyncTask<SensorEvent, Void, SensorEvent> {
    private Context mAppContext;

    public SendDataTask(Context appContext) {
        this.mAppContext = appContext;
    }

    @Override
    protected SensorEvent doInBackground(SensorEvent... params) {
        SensorEvent event = params[0];
        Float data = event.values[0];

        //Send data to server
        RESTService.getInstance(mAppContext).sendData(data, event.sensor.getType());

        //Send broadcast to update the UI if the app is active
        Intent localIntent = new Intent(String.valueOf(event.sensor.getType()));
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_SENSOR_DATA.toString(), data);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mAppContext);
        broadcastManager.sendBroadcast(localIntent);

        return event;
    }
}