package tcslab.syndesiapp.controllers.network;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import tcslab.syndesiapp.controllers.sensor.SensorController;
import tcslab.syndesiapp.models.BroadcastType;

/**
 * Sends data to the server and fire broadcast intents to update the user interface.
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
        RESTInterface.getInstance(mAppContext).sendData(data, event.sensor.getType());

        // Set last sensor value to the controller
        SensorController.getInstance(mAppContext).getmLastSensorValues().put(event.sensor.getType(), data);

        //Send broadcast to update the UI if the app is active
        Intent localIntent = new Intent(String.valueOf(event.sensor.getType()));
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_SENSOR_DATA.toString(), data);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mAppContext);
        broadcastManager.sendBroadcast(localIntent);

        return event;
    }
}