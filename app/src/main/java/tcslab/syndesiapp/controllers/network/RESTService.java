package tcslab.syndesiapp.controllers.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.sensor.SensorList;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.NodeDevice;
import tcslab.syndesiapp.models.NodeType;
import tcslab.syndesiapp.models.PreferenceKey;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tcslab.syndesiapp.views.NodesControllerActivity;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements a REST service in a singleton class to send data to the server
 * Created by Blaise on 04.05.2015.
 */
public class RESTService {
    private static RESTService mInstance;
    private Context mAppContext;
    private RequestQueue mRequestQueue;

    public RESTService(Context appContext) {
        mAppContext = appContext;
        mRequestQueue = this.getRequestQueue();
    }

    public static synchronized RESTService getInstance(Context appContext) {
        if (mInstance == null) {
            mInstance = new RESTService(appContext);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mAppContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Sends data to the server
     *
     * @param data     the data to send
     * @param dataType the type of sensor used to collect the data
     */
    public void sendData(Float data, int dataType) {
        String server_url = PreferenceManager.getDefaultSharedPreferences(mAppContext).getString(PreferenceKey.PREF_SERVER_URL.toString(), "");

        if (!server_url.equals("")) {
            // Instantiate the RequestQueue.
            if (server_url.length() > 7 && !server_url.substring(0, 7).equals("http://")) {
                server_url = "http://" + server_url;
            }
            String id = Secure.getString(mAppContext.getContentResolver(), Secure.ANDROID_ID);
            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

            final String url = server_url + "/api/insertValueCrowd.php?node_name=" + id + "&resource_name="+ SensorList.getStringType(dataType) +
                    "+at+" + id + "&value=" + data + "&unit=" + SensorList.getStringUnit(dataType) + "&timestamp=" +
                    timestamp + "&relative_position=" + "0";

//            JSONObject dataJSON = mAccountController.formatDataJSON(data, dataType);
            JSONObject dataJSON = null;
            //TODO remake JSON data

            // Request a string response from the provided URL.
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, dataJSON,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("HTTP", response.toString());
                            //Send broadcast to update the UI if the app is active
                            RESTService.sendServerStatusBcast(mAppContext, response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", error.toString());
                    Log.d("HTTP", "Error connecting to server address " + url);
                    //Send broadcast to update the UI if the app is active
                    RESTService.sendServerStatusBcast(mAppContext, mAppContext.getString(R.string.connection_error) + ": " + url);
                }
            });

            mRequestQueue.add(request);
        } else {
            RESTService.sendServerStatusBcast(mAppContext, mAppContext.getString(R.string.connection_no_server_set));
        }
    }

    /**
     * Get all the nodes registered on the server
     */
    public void fetchNodes() {
        // Get the sever address from the preferences
        String server_url = PreferenceManager.getDefaultSharedPreferences(mAppContext).getString(PreferenceKey.PREF_SERVER_URL.toString(), "");
        // TEST URL
        server_url = "http://129.194.69.178:8111";

        if (!server_url.equals("")) {
            // Instantiate the RequestQueue.
            if (server_url.length() > 7 && !server_url.substring(0, 7).equals("http://")) {
                server_url = "http://" + server_url;
            }

            // Check server type
            if(PreferenceManager.getDefaultSharedPreferences(mAppContext).getString(PreferenceKey.PREF_SERVER_TYPE.toString(),"").equals("syndesi")) {
                final String url = server_url + "/ero2proxy/service";

                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("HTTP", response);

                        try {
                            Log.d("HTTP", response);

                            // Convert the string response to JSON
                            JSONObject jsonResponse = new JSONObject(response);

                            // Get all the nodes
                            JSONArray nl = jsonResponse.getJSONArray("services");

                            for (int i = 0; i < nl.length(); i++) {
                                JSONObject ns = nl.getJSONObject(i);
                                JSONArray nr = ns.getJSONArray("resources");
                                JSONObject n = nr.getJSONObject(0);

                                // Add the node to the UI
                                NodeType nodeType = NodeType.getType(n.getJSONObject("resourcesnode").getString("name"));
                                ((NodesControllerActivity) mAppContext).addNode(new NodeDevice(n.getString("node_id"), nodeType, nodeType.getStatus(n.getJSONObject("resourcesnode").getString("actuation_state"))));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Update the UI with the error message
                        Log.d("HTTP", "Error connecting to server address " + url);
                        RESTService.sendControllerStatusBcast(mAppContext, mAppContext.getString(R.string.connection_error) + ": " + url);
                    }
                });

                mRequestQueue.add(request);
            }else{
                final String url = server_url + "/api/getNodes.php";

                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("HTTP", response);

                        try {
                            // Convert the string response to JSON
                            JSONObject jsonResponse = new JSONObject(response);

                            // Get all the nodes
                            JSONArray nl = jsonResponse.getJSONArray("Nodes");

                            for (int i = 0; i < nl.length(); i++) {
                                JSONObject n = nl.getJSONObject(i);

                                // Add the node to the UI
                                NodeType nodeType = NodeType.getType(n.getString("name"));
                                ((NodesControllerActivity) mAppContext).addNode(new NodeDevice(n.getString("name"), nodeType, nodeType.getStatus(n.getString("actuator1_state"))));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Update the UI with the error message
                        Log.d("HTTP", "Error connecting to server address " + url);
                        RESTService.sendControllerStatusBcast(mAppContext, mAppContext.getString(R.string.connection_error) + ": " + url);
                    }
                });

                mRequestQueue.add(request);
            }

        } else {
            RESTService.sendControllerStatusBcast(mAppContext, mAppContext.getString(R.string.connection_no_server_set));
        }

        }

    /**
     * Toggle the node given in attribute
     */
    public void toggleNode(final NodeDevice node) {
        Log.d("TODO", "Node toggler not implemented");
    }

    /**
     * Sends the server status in a broadcast to update the user interface
     *
     * @param context the application context
     * @param status  the server status
     */
    public static void sendServerStatusBcast(Context context, String status) {
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
    public static void sendControllerStatusBcast(Context context, String status) {
        //Send broadcast to update the UI if the app is active
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_CONTROLLER_STATUS.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_SERVER_RESPONSE.toString(), status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    public void setmAppContext(Context appContext) {
        this.mAppContext = appContext;
    }
}
