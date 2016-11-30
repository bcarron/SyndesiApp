package tcslab.syndesiapp.controllers.localization;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blais on 30.11.2016.
 */
public class LocalizationController {
    private static LocalizationController mInstance;
    Context mAppContext;
    static final int READ_BLOCK_SIZE = 100;
    File file;


    public int numberAN;
    private List<double[]> samplesFloorList=new ArrayList<double[]>();
    private double[][] samplesFloor;
    int numberSamples, numberAttributes;
    KNearest knn;
    SVM svm;
    String fileName = "rssData.txt";
    private double[] currentLocation;

    // Geneve AP MAC address
    /*private final static String[] anchorNodes={
            "2c:56:dc:d2:06:a8",
            "9c:5c:8e:c5:fb:a0",
            "9c:5c:8e:c5:f1:1a",
            "9c:5c:8e:c5:fb:7a",
            "9c:5c:8e:c5:fb:a6"
    };*/

    // Bussigny AP MAC address
    private final static String[] mAnchorNodes={
            "1c:87:2c:67:80:3c",
            "88:f7:c7:44:fb:40",
            "1c:87:2c:67:80:3c",
            "a4:52:6f:a5:45:11",
            "4e:66:41:fd:26:47"
    };







    public LocalizationController(Context mAppContext) {
        this.mAppContext = mAppContext;
        this.checkFile();
        currentLocation = new double[mAnchorNodes.length];
        for(int i=0; i < mAnchorNodes.length; i++){
            currentLocation[i] = 0;
        }
    }

    public static synchronized LocalizationController getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new LocalizationController(activity);
        }
        return mInstance;
    }

    public double updateLocation(List<ScanResult> APsList){
        ScanResult scanResult;
        String test = "";

        for (int i=0; i<APsList.size(); i++) {
            scanResult = APsList.get(i);

            //search by SSID or MAC
            for(int j=0; j < mAnchorNodes.length; j++){
                if(scanResult.BSSID.equals(mAnchorNodes[j])){
                    currentLocation[j] = scanResult.level;
                    test += " " + Integer.toString(scanResult.level);
                }
            }
        }
        return this.checkFloorSVN(this.currentLocation);
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
                //Put samples in a double[][] array before training
                samplesFloor=new double[samplesFloorList.size()][mAnchorNodes.length + 1];
                int rowCount=0;
                for(double[] sample:samplesFloorList){
                    samplesFloor[rowCount]=sample;
                    rowCount++;
                }

                //Once training samples are uploaded, train machine learning
                numberAttributes = mAnchorNodes.length;
                numberSamples = samplesFloor.length;
                matTrainD = new Mat(numberSamples,numberAttributes, CvType.CV_32F);//Training set rows, cols, type
                matTrainL = new Mat(numberSamples,1,CvType.CV_32SC1); //the same number of rows
                matTest = new Mat(1,numberAttributes,CvType.CV_32F);//Set to test
                //Training data are int, labels are double
                for (int i = 0; i < numberSamples; i++) {
                    for (int j = 0; j <= numberAttributes; j++) {
                        matTrainD.put(i, j, samplesFloor[i][j]);
                        if (j == numberAttributes) { //fill the label
                            matTrainL.put(i, 0,(int)samplesFloor[i][j]);
                        }
                    }
                }

                knn.train(matTrainD, Ml.ROW_SAMPLE, matTrainL);
                svm.setType(SVM.C_SVC);
                svm.setKernel(SVM.LINEAR);
                svm.setGamma(3);
                svm.train(matTrainD, Ml.ROW_SAMPLE, matTrainL);

                Mat centers=new Mat();
                TermCriteria criteria=new TermCriteria(TermCriteria.COUNT,100,1);
                Core.kmeans(matTrainD,matTrainL.rows(),matTrainL,criteria,1,Core.KMEANS_PP_CENTERS,centers);
            }
            catch (Exception es)
            {
                Log.d("Error read samples:  ", es.getMessage());
            }
            breader.close();
        }
        catch (IOException e) {
            toaster("Error reading file: "+e.getMessage());
        }

    }


    //********Machine learning class*****//
    private Mat matTrainD, matTrainL, matTest, matResp;
    public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(mAppContext) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    toaster("OpenCV loaded successfully");
                    knn = KNearest.create();
                    svm = SVM.create();
                    matResp = new Mat(3,1,CvType.CV_32F);// row =number of neighbors
                    //Training process RSSI AND GEOMAGNETIC FIELD
                    readTraining();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public double checkFloorSVN(double[] test){
        //Arrange  Mat to test RSS AND MAGNETIC FIELD
        for(int i=0;i<numberAttributes;i++){
            matTest.put(0,i,test[i]);
        }
        knn.findNearest(matTest, 3, matResp);
        int respS= (int)svm.predict(matTest);
        int respK=(int) matResp.get(0, 0)[0];
        toaster("Location updated - SVM: "+ Integer.toString(respS)+" KNN: "+Integer.toString(respK));
        return respS;  //return SVM response
    }

    public void checkFile(){
        file = new File(mAppContext.getExternalFilesDir(null), fileName);
        if(!file.exists()){
            try {
                FileOutputStream os = new FileOutputStream(file);
                os.close();
            }catch (IOException e){
                toaster("Cannot write data to file");
            }
            toaster("No training file detected");
        }
    }

    public void toaster(String message)
    {
        Toast.makeText(mAppContext, message, Toast.LENGTH_SHORT).show();
    }

    public void checkPosition(View v) {
        double resp=checkFloorSVN(currentLocation);
    }

    public static String[] getmAnchorNodes() {
        return mAnchorNodes;
    }
}
