package tcslab.syndesiapp.controllers.network;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import tcslab.syndesiapp.tools.NodeCallback;
import tcslab.syndesiapp.models.*;
import org.json.JSONObject;

/**
 * Implements a REST service in a singleton class to send data to the server.
 *
 * Created by Blaise on 04.05.2015.
 */
public abstract class RESTInterface {

     public static RESTInterface getInstance(Context appContext){
         RESTInterface restInterface;
         // Check server type
         if(PreferenceManager.getDefaultSharedPreferences(appContext).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
             restInterface = RESTInterfaceSyndesi.getInstance(appContext);
         }else{
             restInterface = RESTInterfaceSengen.getInstance(appContext);
         }

         return restInterface;
     }

    /**
     * Sends data to the server
     *
     * @param data     the data to send
     * @param dataType the type of sensor used to collect the data
     */
    public abstract void sendData(Float data, int dataType);

    /**
     * Get all the nodes registered on the server
     */
    public abstract void fetchNodes(NodeCallback callback);

    /**
     * Toggle the node given in attribute
     */
    public abstract void toggleNode(final NodeDevice node, NodeCallback callback);

    /**
     * Creates the current user account on the server
     *
     * @param account the account to create
     */
    public abstract void createAccount(JSONObject account);

    /**
     * Updates the current account on the server
     *
     * @param account the account to update
     */
    public abstract void updateAccount(JSONObject account);

    public abstract void setmAppContext(Context appContext);

    /**
     * Sends the server status in a broadcast to update the user interface
     *
     * @param context the application context
     * @param status  the server status
     */
    static void sendServerStatusBcast(Context context, String status) {
        //Send broadcast to update the UI if the app is active
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_SERVER_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_SERVER_RESPONSE.toString(), status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    /**
     * Sends the server controller status in a broadcast to update the user interface
     *
     * @param context the application context
     * @param status  the server status
     */
    static void sendControllerStatusBcast(Context context, String status) {
        //Send broadcast to update the UI if the app is active
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_CONTROLLER_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_SERVER_RESPONSE.toString(), status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    /**
     * Convert the office letter found in the service parameter of Syndesi to a real office number
     *
     * @param service the service string from Syndesi
     * @return the office number corresponding to the service
     */
    static String convertOfficeFromService(String service){
        String office = String.valueOf(service.charAt(3));

        switch (office) {
            case "A":
                office = "1.0";
                break;
            case "B":
                office = "2.0";
                break;
            case "C":
                office = "3.0";
                break;
            case "D":
                office = "4.0";
                break;
        }

        return office;
    }
}
