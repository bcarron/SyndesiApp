package tcslab.syndesiapp.models;

import java.util.Date;

/**
 * Represents one WiFI scan
 *
 * Created by Blaise on 07.05.2017.
 */
public class WifiScan {
    private String mRoom;
    private String mResult;
    private Date mTimestamp;

    public WifiScan(String mRoom, String mResult, Date mTimestamp) {
        this.mRoom = mRoom;
        this.mResult = mResult;
        this.mTimestamp = mTimestamp;
    }

    public String getmRoom() {
        return mRoom;
    }

    public String getmResult() {
        return mResult;
    }

    public Date getmTimestamp() {
        return mTimestamp;
    }
}
