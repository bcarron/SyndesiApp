package tcslab.syndesiapp.controllers.automation;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.Sensor;
import android.util.Log;
import tcslab.syndesiapp.controllers.network.RESTInterface;
import tcslab.syndesiapp.controllers.sensor.SensorController;
import tcslab.syndesiapp.models.NodeDevice;
import tcslab.syndesiapp.models.NodeType;

import java.util.ArrayList;

/**
 * Created by blais on 23.02.2017.
 */
public class AutomationController extends ContextWrapper implements NodeCallback{
    private static AutomationController mInstance;
    private RESTInterface restInterface;
    private SensorController mSensorController;
    private ArrayList<NodeDevice> mNodeList;

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

    public void changeOffice(String newOffice, String oldOffice){
        enterOffice(newOffice);
        leaveOffice(oldOffice);
    }

    public void enterOffice(String office){
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
                    restInterface.toggleNode(node);
                }
            }
        }
    }

    public void leaveOffice(String office){
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

    @Override
    public void addNodesCallback(ArrayList<NodeDevice> nodesList) {
        this.mNodeList.addAll(nodesList);
    }
}
