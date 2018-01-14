package tcslab.syndesiapp.tools;

import tcslab.syndesiapp.models.NodeDevice;

import java.util.ArrayList;

/**
 * Callback class to manage the list of nodes connected to the Syndesi server
 *
 * Created by Blaise on 23.02.2017.
 */
public interface NodeCallback {
    /**
     * Callback method to be called when the list of nodes is available
     *
     * @param nodesList the full list of nodes
     */
    void addNodesCallback(ArrayList<NodeDevice> nodesList);

    /**
     * Callback method when a node is updated
     *
     * @param node the node to update
     */
    void addNode(NodeDevice node);
}