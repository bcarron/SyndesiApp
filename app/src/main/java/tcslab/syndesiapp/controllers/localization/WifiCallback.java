package tcslab.syndesiapp.controllers.localization;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by blais on 06.05.2017.
 */
public interface WifiCallback {
    void sendResults(List<ScanResult> readings);
}
