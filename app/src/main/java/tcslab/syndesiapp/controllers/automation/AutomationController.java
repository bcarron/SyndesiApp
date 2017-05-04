package tcslab.syndesiapp.controllers.automation;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.hardware.Sensor;
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
    private ArrayList<NodeDevice> mNodeList;
    private String mCurrentPosition;

    public AutomationController(Context base) {
        super(base);

        restInterface = RESTInterface.getInstance(this);
        mSensorController = SensorController.getInstance(null);
        mNodeList = new ArrayList<>();

        restInterface.fetchNodes(this);
    }

    public static synchronized AutomationController getInstance(Context appContext) {
        if (mInstance == null) {
            mInstance = new AutomationController(appContext);
        }
        return mInstance;
    }

    public void updatePosition(String newPosition){
        if(mCurrentPosition != null && !mCurrentPosition.equals(newPosition)) {
            enterOffice(newPosition);
            leaveOffice(mCurrentPosition);
        }else{
            updateUI(this.getString(R.string.automation_no_change), R.id.automation_display_status);
            updateUI("", R.id.automation_display_new_position);
            updateUI("", R.id.automation_display_old_position);
        }

        mCurrentPosition = newPosition;
    }

    public void enterOffice(String office){
        updateUI("You are entering a new office", R.id.automation_display_status);
        updateUI("Entering office " + office + ": turning the lights on!", R.id.automation_display_new_position);
        Log.d("Automation", "You are entering a new office: turning the lights on!");

        for(NodeDevice node : mNodeList){
            // Check if the node is in the new office
            if(node.getmOffice().equals(office)){
                // If the node is a light and is off, turn it on
                if(node.getmType() == NodeType.light && node.getmStatus().equals("off")){
                    Log.d("Automation", "Turning lights on");
                    restInterface.toggleNode(node);
                }

                // If the node is a fan and the temperature is too high, turn it on.
                Float temperature = mSensorController.getmLastSensorValues().get(Sensor.TYPE_AMBIENT_TEMPERATURE);
                if(temperature != null && temperature > 25 && node.getmType() == NodeType.fan && node.getmStatus().equals("off")){
                    Log.d("Automation", "Turning fans on");
                    updateUI("Office " + office + ": temperature too high: turning the fans on!", R.id.automation_display_new_position);
                    restInterface.toggleNode(node);
                }
            }
        }
    }

    public void leaveOffice(String office){
        updateUI("Leaving office " + office + ": turning all appliances off!", R.id.automation_display_old_position);
        Log.d("Automation", "Leaving office " + office + ": turning all appliances off!");


        for(NodeDevice node : mNodeList){
            // Check if the node is in the old office
            if(node.getmOffice().equals(office)){
                // If the node is a light and is on, turn it off
                if(node.getmType() == NodeType.light && node.getmStatus().equals("on")){
                    restInterface.toggleNode(node);
                }

                // If the node is a fan and is on, turn it off
                if(node.getmType() == NodeType.fan && node.getmStatus().equals("on")){
                    restInterface.toggleNode(node);
                }
            }
        }
    }

    private void updateUI(String status, int id){
        // Send broadcast to update the UI
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_AUT_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_AUT_DISP.toString(), id);
        localIntent.putExtra(BroadcastType.BCAST_TYPE_AUT_STATUS.toString(), status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void addNodesCallback(ArrayList<NodeDevice> nodesList) {
        this.mNodeList.addAll(nodesList);
    }
}
