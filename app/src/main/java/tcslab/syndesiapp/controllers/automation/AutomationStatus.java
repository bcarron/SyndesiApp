package tcslab.syndesiapp.controllers.automation;

import tcslab.syndesiapp.models.NodeType;

import java.util.Date;

/**
 * Created by blais on 04.05.2017.
 */
public class AutomationStatus {
    private String mOffice;
    private String mMessage;
    private NodeType mNodeType;
    private String mStatus;
    private Date mTimestamp;

    public AutomationStatus(String mOffice, String mMessage, NodeType mNodeType, String mStatus, Date mTimestamp) {
        this.mOffice = mOffice;
        this.mMessage = mMessage;
        this.mNodeType = mNodeType;
        this.mStatus = mStatus;
        this.mTimestamp = mTimestamp;
    }

    public String getmOffice() {
        return mOffice;
    }

    public String getmStatus() {
        return mStatus;
    }

    public NodeType getmNodeType() {
        return mNodeType;
    }

    public String getmMessage() {
        return mMessage;
    }

    public Date getmTimestamp(){
        return mTimestamp;
    }

    public void setmTimestamp(Date mTimestamp) {
        this.mTimestamp = mTimestamp;
    }
}
