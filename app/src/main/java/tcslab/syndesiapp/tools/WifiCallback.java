package tcslab.syndesiapp.tools;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Callback class to receive WiFi scan results
 *
 * Created by Blaise on 06.05.2017.
 */
public interface WifiCallback {
    /**
     * Callback method to be called when the scan results are available
     * @param readings the results
     */
    void sendResults(List<ScanResult> readings);
}
