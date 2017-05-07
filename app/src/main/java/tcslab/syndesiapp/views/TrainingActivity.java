package tcslab.syndesiapp.views;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.*;
import tcslab.syndesiapp.R;
import tcslab.syndesiapp.controllers.localization.ScanAdapter;
import tcslab.syndesiapp.controllers.localization.WifiCallback;
import tcslab.syndesiapp.controllers.localization.WifiReceiver;
import tcslab.syndesiapp.controllers.localization.WifiScan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by blais on 06.05.2017.
 */
public class TrainingActivity extends AppCompatActivity implements WifiCallback{
    private Boolean isTraining = false;
    private Handler mHandler;
    private WifiReceiver mWifiReceiver;
    private EditText roomNumber;
    private Button mButton;
    private File mFile;
    private String mFileName = "rssData.txt";
    private ScanAdapter mScanAdapter;
    private ArrayList<WifiScan> mScans;
    private int mNbScan = 0;

    // Geneve AP MAC address
/*    private final static String[] mAnchorNodes={
            "2c:56:dc:d2:06:a8",
            "9c:5c:8e:c5:fb:a0",
            "9c:5c:8e:c5:f1:1a",
            "9c:5c:8e:c5:fb:7a",
            "9c:5c:8e:c5:fb:a6"
    };*/

    // Bussigny AP MAC address
    private final static String[] mAnchorNodes={
            "1c:87:2c:67:80:38",
            "1c:87:2c:67:80:3c",
            "88:f7:c7:44:fb:40",
            "08:3e:5d:35:21:29",
            "f8:df:a8:7a:ed:6d"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the layout
        setContentView(R.layout.training);
        roomNumber = (EditText) findViewById(R.id.room_number);
        mButton = (Button) findViewById(R.id.training_button);

        //Set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Account preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Wifi receiver
        mHandler = new Handler();
        mWifiReceiver = new WifiReceiver(this, this);

        //Set the scan list
        ListView listView = (ListView) findViewById(R.id.scan_list);
        mScans = new ArrayList<>();
        mScanAdapter = new ScanAdapter(this, mScans);
        listView.setAdapter(mScanAdapter);

        //Create a new file
        checkFile();
    }

    //Life cycle management
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Make sure the Wifi Receiver is unregistered
        if(isTraining){
            stopTraining();
        }
        super.onPause();
    }

    @Override
    public void sendResults(List<ScanResult> readings) {
        String room = roomNumber.getText().toString();

        if(room != null && !room.equals("")) {
            // Register the result to file
            String result = registerScan(room, readings);

            //Update the UI
            mScans.add(new WifiScan(room, result, new Date()));
            mScanAdapter.notifyDataSetChanged();
            mNbScan++;
            ((TextView) findViewById(R.id.nb_scan)).setText("Number of scans: " + String.valueOf(mNbScan));

            // Launch new scan
            mHandler.post(new StartScan());
        }else{
            toaster("Please enter a room number to start training!");
        }
    }

    public void toggleTraining(View v){
        if(isTraining){
            stopTraining();
        }else{
            if(roomNumber.getText().toString() != null && !roomNumber.getText().toString().equals("")){
                startTraining();
            }else{
                toaster("Please enter a room number to start training!");
            }
        }
    }

    private void startTraining(){
        //Register the Wifi listener
        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        isTraining = true;
        mButton.setText(getString(R.string.form_training_button_stop));

        mNbScan = 0;

        mHandler.post(new StartScan());
    }

    private void stopTraining(){
        //Unregister the Wifi listener
        unregisterReceiver(mWifiReceiver);
        isTraining = false;
        mButton.setText(getString(R.string.form_training_button_start));
    }

    private String registerScan(String room, List<ScanResult> readings){
        // Read the results
        int nbAP = mAnchorNodes.length;
        String[] RSS = new String[nbAP];
        for(ScanResult result: readings){
            for(int i = 0; i < nbAP; i++){
                if(mAnchorNodes[i].equals(result.BSSID)){
                    RSS[i] = Integer.toString(result.level);
                }
            }
        }

        // Register the results
        String line = "";
        for(int i = 0; i < nbAP; i++){
            if(RSS[i] == null){
                RSS[i] = "-99";
            }
            if(i == 0){
                line = RSS[i];
            }
            else{
                line += "\t" + RSS[i];
            }
        }
        line += "\t" + room + "\n\r";
        try {
            if (isTraining) {
                FileWriter w = new FileWriter(mFile, true);
                w.append(line);
                w.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return line;
    }

    private void checkFile(){
        mFile = new File(getExternalFilesDir(null), mFileName);
        if(!mFile.exists()){
            // If the file does not exist, create one
            createFile(new View(this));
        }
    }

    public void createFile(View v){
        try {
            FileOutputStream os = new FileOutputStream(mFile);
            os.close();
            toaster("New training file created");
        } catch (IOException e) {
            toaster("Error creating the new training file!");
        }
    }

    private class StartScan implements Runnable{
        @Override
        public void run(){
            if(isTraining){
                Log.d("Localization", "Start scan");
                ((WifiManager) getSystemService(Context.WIFI_SERVICE)).startScan();
            }
        }
    }

    private void toaster(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
