package tcslab.syndesiapp.controllers.automation;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.network.RESTInterface;
import tcslab.syndesiapp.controllers.sensor.SensorController;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.NodeDevice;
import tcslab.syndesiapp.models.NodeType;
import tcslab.syndesiapp.models.PreferenceKey;

import java.util.ArrayList;

/**
 * Created by blais on 23.02.2017.
 */
public class AutomationController extends ContextWrapper implements NodeCallback{
    private static AutomationController mInstance;
    private RESTInterface restInterface;
    private SensorController mSensorController;
    private SharedPreferences mSharedPreferences;
    private ArrayList<NodeDevice> mNodeList;
    private String mCurrentPosition;

    private AutomationController(Context base) {
        super(base);

        restInterface = RESTInterface.getInstance(this);
        mSensorController = SensorController.getInstance(null);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mNodeList = new ArrayList<>();

        // Get all the nodes connected to the server
        restInterface.fetchNodes(this);
    }

    public static synchronized AutomationController getInstance(Context appContext) {
        if (mInstance == null) {
            mInstance = new AutomationController(appContext);
        }
        return mInstance;
    }

    public void updatePosition(String newPosition){
        Boolean perm = mSharedPreferences.getBoolean(PreferenceKey.PREF_AUT_PERM.toString(), false);
        if(mCurrentPosition != null && !mCurrentPosition.equals(newPosition)) {
            if(perm){
                enterOffice(newPosition);
                leaveOffice(mCurrentPosition);
            }
        }

        mCurrentPosition = newPosition;

        if(perm){
            automation();
        }
    }

    public void automation(){
        Float targetLight = mSharedPreferences.getFloat(PreferenceKey.PREF_TARGET_LIGHT.toString(), 250);
        Float targetTemp = mSharedPreferences.getFloat(PreferenceKey.PREF_TARGET_TEMP.toString(), 22);

        Float temperature = mSensorController.getmLastSensorValues().get(Sensor.TYPE_AMBIENT_TEMPERATURE);
        Float illuminance = mSensorController.getmLastSensorValues().get(Sensor.TYPE_LIGHT);

        // Illuminance automation
        if(illuminance != null && illuminance > targetLight + 10){
            updateUI(mCurrentPosition, "Illuminance too high: turning some lights off!", NodeType.bulb, "off");
            toggleNodes(NodeType.bulb, mCurrentPosition, "on");
        }else if(illuminance != null && illuminance > targetLight + 100){
            updateUI(mCurrentPosition, "Illuminance way too high: bringing down the curtains!", NodeType.curtain, "down");
            toggleNodes(NodeType.curtain, mCurrentPosition, "up");
        }else if(illuminance != null && illuminance < targetLight - 10){
            updateUI(mCurrentPosition, "Illuminance too low: turning some lights on!", NodeType.bulb, "on");
            toggleNodes(NodeType.bulb, mCurrentPosition, "off");
        }else if(illuminance != null && illuminance < targetLight - 100){
            updateUI(mCurrentPosition, "Illuminance way too low: bringing up the curtains!", NodeType.curtain, "up");
            toggleNodes(NodeType.curtain, mCurrentPosition, "down");
        }

        // Temperature automation
        if(temperature != null && temperature > targetTemp + 1){
            updateUI(mCurrentPosition, "Temperature too high: turning some fans on!", NodeType.fan, "on");
            toggleNodes(NodeType.fan, mCurrentPosition, "off");
        }else if(temperature != null && temperature < targetTemp - 1){
            updateUI(mCurrentPosition, "Temperature too low: turning some fans off!", NodeType.fan, "off");
            toggleNodes(NodeType.fan, mCurrentPosition, "on");
        }else if(temperature != null && temperature < targetTemp - 5){
            updateUI(mCurrentPosition, "Temperature way too low: turning the heaters on!", NodeType.fan, "on");
            toggleNodes(NodeType.heater, mCurrentPosition, "off");
        }
    }

    private void enterOffice(String office){
        updateUI(office, "Entering office, turning the lights on!", NodeType.bulb, "on");

        toggleNodes(NodeType.bulb, office, "off");
    }

    private void leaveOffice(String office){
        updateUI(office, "Leaving office, turning all appliances off!", NodeType.bulb, "off");

        toggleNodes(NodeType.bulb, office, "on");
        toggleNodes(NodeType.fan, office, "on");
        toggleNodes(NodeType.heater, office, "on");
    }

    private void toggleNodes(NodeType type, String office, String status){
        for(NodeDevice node : mNodeList){
            if(node.getmOffice().equals(office)){
                if(node.getmType() == type && node.getmStatus().equals(status)){
                    restInterface.toggleNode(node);
                }
            }
        }
    }

    private void updateUI(String office, String message, NodeType nodeType, String status){
        // Send broadcast to update the UI
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_AUT_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_AUT_OFFICE.toString(), office);
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_AUT_MESSAGE.toString(), message);
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_AUT_TYPE.toString(), nodeType.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_AUT_STATUS.toString(), status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void addNodesCallback(ArrayList<NodeDevice> nodesList) {
        this.mNodeList.addAll(nodesList);
    }
}
