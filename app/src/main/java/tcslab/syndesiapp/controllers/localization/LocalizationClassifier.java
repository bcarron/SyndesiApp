package tcslab.syndesiapp.controllers.localization;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;
import tcslab.syndesiapp.models.BroadcastType;
import tcslab.syndesiapp.models.PreferenceKey;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Determine in which office the user is using neighbour WiFi access points by using two classifiers (SVM + KNN).
 *
 * Created by Blaise on 14.12.2016.
 */
public class LocalizationClassifier {
    private static LocalizationClassifier mInstance;
    private WifiService mWifiService;
    private SharedPreferences mSharedPreferences;
    static final int READ_BLOCK_SIZE = 100;
    private File file;
    private String mCurrentPosition;
    private int nbReadings = 0;
    private Boolean toastPermission = false;


    public int numberAN;
    private List<double[]> samplesFloorList=new ArrayList<double[]>();
    private double[][] samplesFloor;
    private int numberSamples, numberAttributes;
    private KNearest knn;
    private SVM svm;
    private String fileName = "rssData.txt";
    private double[] mRSSIs;

    // Geneve AP MAC address
    private final static String[] mAnchorNodes={
            "2c:56:dc:d2:06:a8",
            "9c:5c:8e:c5:fb:a0",
            "9c:5c:8e:c5:f1:1a",
            "9c:5c:8e:c5:fb:7a",
            "9c:5c:8e:c5:fb:a6"
    };

    // Bussigny AP MAC address
    /*private final static String[] mAnchorNodes={
            "1c:87:2c:67:80:38",
            "1c:87:2c:67:80:3c",
            "88:f7:c7:44:fb:40",
            "08:3e:5d:35:21:29",
            "f8:df:a8:7a:ed:6d"
    };*/


    public LocalizationClassifier(WifiService wifiService) {
        this.mWifiService = wifiService;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mWifiService);
        checkFile();
        mRSSIs = new double[mAnchorNodes.length];
        for(int i=0; i < mAnchorNodes.length; i++){
            mRSSIs[i] = 0;
        }

        /* Load OpenCV */
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "  OpenCVLoader.initDebug(), not working.");
        } else {
            this.mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public static synchronized LocalizationClassifier getInstance(WifiService wifiService) {
        if (mInstance == null) {
            mInstance = new LocalizationClassifier(wifiService);
        }
        return mInstance;
    }

    public String updateLocation(List<List<ScanResult>> readings){
        HashMap<Double, Integer> results = new HashMap<>();
        ScanResult scanResult;
        int nb_classifications = 0;

        for(List<ScanResult> APsList : readings) {
            for (int i = 0; i < APsList.size(); i++) {
                scanResult = APsList.get(i);

                //search by SSID or MAC
                for (int j = 0; j < mAnchorNodes.length; j++) {
                    if (scanResult.BSSID.equals(mAnchorNodes[j])) {
                        mRSSIs[j] = scanResult.level;
                    }
                }
            }

            HashMap<String, Double> response = checkFloorSVN(mRSSIs);

            if(response == null){
                return null;
            }else {
                for(String classifier : response.keySet()){
                    if(mSharedPreferences.getString(PreferenceKey.PREF_CLASSIFIER_TYPE.toString(),"").equals(classifier) ||
                            mSharedPreferences.getString(PreferenceKey.PREF_CLASSIFIER_TYPE.toString(),"").equals("all")) {
                        nb_classifications++;
                        if (results.containsKey(response.get(classifier))) {
                            results.put(response.get(classifier), results.get(response.get(classifier)) + 1);
                        } else {
                            results.put(response.get(classifier), 1);
                        }
                    }
                }
            }
        }

        double maxPosition = -1;
        String toastMessage = "";
        for(Double result : results.keySet()){
            toastMessage += "Room " + result + " (" + results.get(result) + "/" + nb_classifications + "), ";
            if(maxPosition == -1 || results.get(result) > results.get(maxPosition)){
                maxPosition = result;
            }
        }

        toastMessage = toastMessage.substring(0, toastMessage.length() - 2);
//        mWifiService.toaster(toastMessage, Toast.LENGTH_LONG);
        Log.d("Localization", "Room " + maxPosition + " (" + results.get(maxPosition) + " classifications)");
        mWifiService.toaster("Room " + maxPosition, Toast.LENGTH_SHORT);

        mCurrentPosition = Double.toString(maxPosition);

        updateUI();

        return mCurrentPosition;
    }

    // Read text from file and train machine learning approaches
    public void readTraining() {
        try {
            // FileReader reader = new FileReader(file);
            BufferedReader breader = new BufferedReader(new FileReader(file));
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            String[] features;
            try {
                while ((s = breader.readLine()) != null) {
                    features = s.split("\t");
                    if(features.length!=1) {
                        int length = features.length;

                        double[] featuresT = new double[length];
                        for (int column = 0; column < length; column++) {
                            featuresT[column] = Double.parseDouble(features[column]);
                        }
                        samplesFloorList.add(featuresT);
                    }

                }
                if(samplesFloorList.size() > 0) {
                    //Put samples in a double[][] array before training
                    samplesFloor = new double[samplesFloorList.size()][mAnchorNodes.length + 1];
                    int rowCount = 0;
                    for (double[] sample : samplesFloorList) {
                        samplesFloor[rowCount] = sample;
                        rowCount++;
                    }

                    //Once training samples are uploaded, train machine learning
                    numberAttributes = mAnchorNodes.length;
                    numberSamples = samplesFloor.length;
                    matTrainD = new Mat(numberSamples, numberAttributes, CvType.CV_32F);//Training set rows, cols, type
                    matTrainL = new Mat(numberSamples, 1, CvType.CV_32SC1); //the same number of rows
                    matTest = new Mat(1, numberAttributes, CvType.CV_32F);//Set to test
                    //Training data are int, labels are double
                    for (int i = 0; i < numberSamples; i++) {
                        for (int j = 0; j <= numberAttributes; j++) {
                            matTrainD.put(i, j, samplesFloor[i][j]);
                            if (j == numberAttributes) { //fill the label
                                matTrainL.put(i, 0, (int) samplesFloor[i][j]);
                            }
                        }
                    }

                    knn.train(matTrainD, Ml.ROW_SAMPLE, matTrainL);
                    svm.setType(SVM.C_SVC);
                    svm.setKernel(SVM.LINEAR);
                    svm.setGamma(3);
                    svm.train(matTrainD, Ml.ROW_SAMPLE, matTrainL);

                    Mat centers = new Mat();
                    TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
                    Core.kmeans(matTrainD, matTrainL.rows(), matTrainL, criteria, 1, Core.KMEANS_PP_CENTERS, centers);
                }
            }
            catch (Exception es)
            {
                Log.d("Localization", es.getMessage());
            }
            breader.close();
        }
        catch (IOException e) {
            Log.d("Localization", e.getMessage());
        }

    }


    //********Machine learning class*****//
    private Mat matTrainD, matTrainL, matTest, matResp;
    public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(mWifiService) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    knn = KNearest.create();
                    svm = SVM.create();
                    matResp = new Mat(3,1,CvType.CV_32F);// row =number of neighbors
                    //Training process RSSI AND GEOMAGNETIC FIELD
                    if(file.exists()) {
                        readTraining();
                    }else{
                        mWifiService.toaster("Cannot train the classifiers: missing training file");
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public HashMap<String, Double> checkFloorSVN(double[] test){
        if(file.exists()) {
            HashMap<String, Double> response = new HashMap<>();
            //Arrange  Mat to test RSS AND MAGNETIC FIELD
            for (int i = 0; i < numberAttributes; i++) {
                matTest.put(0, i, test[i]);
            }

            // KNN
            if(mSharedPreferences.getString(PreferenceKey.PREF_CLASSIFIER_TYPE.toString(),"").equals("knn") ||
                    mSharedPreferences.getString(PreferenceKey.PREF_CLASSIFIER_TYPE.toString(),"").equals("all")) {
                knn.findNearest(matTest, 3, matResp);
                response.put("knn", matResp.get(0, 0)[0]);
            }

            // SVM
            if(mSharedPreferences.getString(PreferenceKey.PREF_CLASSIFIER_TYPE.toString(),"").equals("svm") ||
                    mSharedPreferences.getString(PreferenceKey.PREF_CLASSIFIER_TYPE.toString(),"").equals("all")) {
                response.put("svm", (double) svm.predict(matTest));
            }

            return response;  //return both response
        }
        return null;
    }

    public void checkFile(){
        file = new File(mWifiService.getExternalFilesDir(null), fileName);
        if(!file.exists()){
            // If the file does not exist, write the default file to memory
            mWifiService.toaster("Using default training file");
            Log.d("Localization", "Using default training file");

            try {
                InputStream is = mWifiService.getAssets().open("default_training.txt");
                FileOutputStream os = new FileOutputStream(file);
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();

                os.write(buffer);
                os.close();
            } catch (IOException e) {
                mWifiService.toaster("Missing training file");
                Log.e("Localization", "Missing training file");
            }
        }
    }

    private void updateUI(){
        // Send broadcast to update the UI
        Intent localIntent = new Intent(BroadcastType.BCAST_TYPE_LOC_POSITION.toString());
        localIntent.putExtra(BroadcastType.BCAST_EXTRA_LOC_OFFICE.toString(), mCurrentPosition);
        LocalBroadcastManager.getInstance(mWifiService).sendBroadcast(localIntent);
    }

    public String getmCurrentPosition() {
        return mCurrentPosition;
    }

    public Boolean getToastPermission() {
        return toastPermission;
    }

    public void setToastPermission(Boolean toastPermission) {
        toastPermission = toastPermission;
    }
}
