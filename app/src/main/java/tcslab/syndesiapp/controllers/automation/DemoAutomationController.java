package tcslab.syndesiapp.controllers.automation;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import tcslab.syndesiapp.tools.NodeCallback;
import tcslab.syndesiapp.controllers.network.RESTInterface;
import tcslab.syndesiapp.controllers.sensor.SensorController;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.NodeDevice;
import tcslab.syndesiapp.models.NodeType;
import tcslab.syndesiapp.models.PreferenceKey;

import java.util.ArrayList;

/**
 * Manages the appliances automatically according to the user's preferences
 * Created specifically for the LCN 2017 demo
 *
 * Created by Blaise on 23.02.2017.
 */
public class DemoAutomationController extends ContextWrapper implements NodeCallback {
    private static DemoAutomationController mInstance;
    private RESTInterface restInterface;
    private SensorController mSensorController;
    private SharedPreferences mSharedPreferences;
    private ArrayList<NodeDevice> mNodeList;
    private String mCurrentPosition;

    private DemoAutomationController(Context base) {
        super(base);

        restInterface = RESTInterface.getInstance(this);
        mSensorController = SensorController.getInstance(null);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mNodeList = new ArrayList<>();

        // Get all the nodes connected to the server
        restInterface.fetchNodes(this);
    }

    public static synchronized DemoAutomationController getInstance(Context appContext) {
        if (mInstance == null) {
            mInstance = new DemoAutomationController(appContext);
        }
        return mInstance;
    }

    /**
     * Updates the position in the controller
     *
     * @param newPosition the new position
     */
    public void updatePosition(String newPosition){
        Boolean perm = mSharedPreferences.getBoolean(PreferenceKey.PREF_AUT_PERM.toString(), false);
        if(newPosition.equals("1.0")) {
            if(perm){
                enterOffice("1.0");
            }
        }else if(!newPosition.equals("1.0")){
            if(perm){
                leaveOffice("1.0");
            }
        }

        mCurrentPosition = newPosition;

        if(perm){
            automation();
        }
    }

    /**
     * Manages the appliance
     */
    public void automation(){
        Log.d("Automation", "No temp/light automation in demo");
        // No Automation in Demo
    }

    /**
     * Manages appliances when a user enters a new office
     *
     * @param office the new office
     */
    private void enterOffice(String office){
        updateUI(office, "Entering office, turning the lights on!", NodeType.bulb, "on");

        toggleNodes(NodeType.sengen, office, "off");
        toggleNodes(NodeType.bulb, office, "off");
    }

    /**
     * Manages appliances when the user leaves an office
     *
     * @param office the old office
     */
    private void leaveOffice(String office){
        updateUI(office, "Leaving office, turning all appliances off!", NodeType.bulb, "off");

        toggleNodes(NodeType.sengen, office, "on");
        toggleNodes(NodeType.bulb, office, "on");
        toggleNodes(NodeType.fan, office, "on");
        toggleNodes(NodeType.heater, office, "on");
    }

    /**
     * Toggle the state of nodes
     *
     * @param type the type of node to be toggled
     * @param office the office where the nodes need to be toggled
     * @param status the status of the nodes to be toggled
     */
    private void toggleNodes(NodeType type, String office, String status){
        for(NodeDevice node : mNodeList){
            if(node.getmOffice().equals(office)){
                if(node.getmType() == type && node.getmStatus().equals(status)){
                    restInterface.toggleNode(node, this);
                }
            }
        }
    }

    /**
     * Update the UI
     *
     * @param office the current office
     * @param message the message to display
     * @param nodeType the node type
     * @param status the status of the nodes
     */
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

    @Override
    public void addNode(NodeDevice node){
        Boolean nodeExist = false;
        for(NodeDevice currentNode : mNodeList){
            if(currentNode.getmNID().equals(node.getmNID())) {
                currentNode.setmStatus(node.getmStatus());
                nodeExist = true;
            }
        }
        if(!nodeExist){
            mNodeList.add(node);
        }
    }
}
