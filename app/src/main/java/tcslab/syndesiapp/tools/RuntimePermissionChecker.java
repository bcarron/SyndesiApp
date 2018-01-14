package tcslab.syndesiapp.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Check the permission at runtime for Android 6+.
 *
 * Created by Blaise on 05.12.2016.
 */
public class RuntimePermissionChecker {
    private Activity mActivity;
    private String[] permissionNeeded = new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public RuntimePermissionChecker(Activity activity){
        this.mActivity = activity;
    }


    /**
     * Get all the permissions needed to run the app
     * @return wether the permissions were granted or not
     */
    public Boolean getPermissions(){
        if (hasPermission(permissionNeeded)) {
            return true;
        } else {
            ActivityCompat.requestPermissions(mActivity, permissionNeeded, 123);
            return false;
        }
    }

    /**
     * Check if the app has sufficient permissions
     *
     * @param permissions the permissions needed to run the app
     * @return wether or not the app has sufficient permissions
     */
    private Boolean hasPermission(String[] permissions){
        for(String perm : permissions){
            if (ContextCompat.checkSelfPermission(mActivity, perm) == PackageManager.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }
}
