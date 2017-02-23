package tcslab.syndesiapp.controllers.automation;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import tcslab.syndesiapp.controllers.network.RESTService;
import tcslab.syndesiapp.models.NodeDevice;
import java.util.ArrayList;

/**
 * Created by blais on 23.02.2017.
 */
public class AutomationController extends ContextWrapper implements NodeCallback{
    private static AutomationController mInstance;
    private RESTService restService;
    private ArrayList<NodeDevice> mNodeList;

    public AutomationController(Context base) {
        super(base);

        restService = RESTService.getInstance(this);
        mNodeList = new ArrayList<>();

        restService.fetchNodes(this);
    }

    public void enterOffice(String office){
        for(NodeDevice node : mNodeList){
            Log.d("Automation", node.toString());
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
