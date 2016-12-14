package tcslab.syndesiapp.controllers.network;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import tcslab.syndesiapp.models.*;
import org.json.JSONObject;

/**
 * Implements a REST service in a singleton class to send data to the server.
 *
 * Created by Blaise on 04.05.2015.
 */
public abstract class RESTService {

     public static RESTService getInstance(Context appContext){
         RESTService restService;
         // Check server type
         if(PreferenceManager.getDefaultSharedPreferences(appContext).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
             restService = RESTServiceSyndesi.getInstance(appContext);
         }else{
             restService = RESTServiceSengen.getInstance(appContext);
         }

         return restService;
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
    public abstract void fetchNodes();

    /**
     * Toggle the node given in attribute
     */
    public abstract void toggleNode(final NodeDevice node);

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
}
