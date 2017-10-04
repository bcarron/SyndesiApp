package tcslab.syndesiapp.controllers.automation;

import tcslab.syndesiapp.models.NodeDevice;

import java.util.ArrayList;

/**
 * Created by blais on 23.02.2017.
 */
public interface NodeCallback {
    void addNodesCallback(ArrayList<NodeDevice> nodesList);
    void addNode(NodeDevice node);
}