package tcslab.syndesiapp.controllers.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.localization.LocalizationController;
import tcslab.syndesiapp.controllers.sensor.SensorList;
import tcslab.syndesiapp.models.NodeDevice;
import tcslab.syndesiapp.models.NodeType;
import tcslab.syndesiapp.models.PreferenceKey;
import tcslab.syndesiapp.views.NodesControllerActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by blais on 23.11.2016.
 */
public class RESTServiceSengen extends RESTService{
    private static RESTServiceSengen mInstance;
    private Context mAppContext;
    private RequestQueue mRequestQueue;
    private SharedPreferences mPreferences;

    public RESTServiceSengen(Context appContext) {
        mAppContext = appContext;
        mRequestQueue = this.getRequestQueue();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    public static synchronized RESTServiceSengen getInstance(Context appContext) {
        if (mInstance == null) {
            mInstance = new RESTServiceSengen(appContext.getApplicationContext());
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
        String server_url = mPreferences.getString(PreferenceKey.PREF_SENGEN_URL.toString(), "");

        if (!server_url.equals("")) {
            // Instantiate the RequestQueue.
            if (server_url.length() > 7 && !server_url.substring(0, 7).equals("http://")) {
                server_url = "http://" + server_url;
            }

            String id = Settings.Secure.getString(mAppContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            String position = mPreferences.getString(PreferenceKey.PREF_CURRENT_POSITION.toString(), null);

            final String url = server_url + "/api/insertValueCrowd.php?node_name=" + id + "&resource_name=" +
                    SensorList.getStringType(dataType) + "+at+" + id + "&value=" + data + "&unit=" +
                    SensorList.getStringUnit(dataType) + "&timestamp=" + timestamp + "&relative_position=" + position;

            // Request a string response from the provided URL.
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
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
        String server_url = mPreferences.getString(PreferenceKey.PREF_SENGEN_URL.toString(), "");

        if (!server_url.equals("")) {
            // Instantiate the RequestQueue.
            if (server_url.length() > 7 && !server_url.substring(0, 7).equals("http://")) {
                server_url = "http://" + server_url;
            }

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

        } else {
            RESTService.sendControllerStatusBcast(mAppContext, mAppContext.getString(R.string.connection_no_server_set));
        }

    }

    /**
     * Toggle the node given in attribute
     */
    public void toggleNode(final NodeDevice node) {
        // Get the sever address from the preferences
        String server_url = mPreferences.getString(PreferenceKey.PREF_SENGEN_URL.toString(), "");

        if (!server_url.equals("")) {
            // Instantiate the RequestQueue.
            if (server_url.length() > 7 && !server_url.substring(0, 7).equals("http://")) {
                server_url = "http://" + server_url;
            }

            Log.e("TODO", "Node toggler not implemented for Sengen DB");
        }
    }

    public void createAccount(JSONObject account){
        Log.d("Sengen", "No account with Sengen DB");
    }


    public void updateAccount(JSONObject account){
        Log.d("Sengen", "No account with Sengen DB");
    }

    public void setmAppContext(Context appContext){
        this.mAppContext = appContext;
    }
}
