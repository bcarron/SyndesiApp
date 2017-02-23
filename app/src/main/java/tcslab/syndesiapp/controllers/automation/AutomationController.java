package tcslab.syndesiapp.controllers.automation;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import tcslab.syndesiapp.controllers.network.RESTInterface;
import tcslab.syndesiapp.models.NodeDevice;
import tcslab.syndesiapp.models.NodeType;

import java.util.ArrayList;

/**
 * Created by blais on 23.02.2017.
 */
public class AutomationController extends ContextWrapper implements NodeCallback{
    private static AutomationController mInstance;
    private RESTInterface restInterface;
    private ArrayList<NodeDevice> mNodeList;

    public AutomationController(Context base) {
        super(base);

        restInterface = RESTInterface.getInstance(this);
        mNodeList = new ArrayList<>();

        restInterface.fetchNodes(this);
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
            }
        }
    }

    public static synchronized AutomationController getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new AutomationController(activity);
        }
        return mInstance;
    }

    @Override
    public void addNodesCallback(ArrayList<NodeDevice> nodesList) {
        this.mNodeList.addAll(nodesList);
    }
}
