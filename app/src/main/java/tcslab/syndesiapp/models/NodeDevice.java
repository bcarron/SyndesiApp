package tcslab.syndesiapp.models;

/**
 * Represents a node connected to the system.
 *
 * Created by Blaise on 31.05.2015.
 */
public class NodeDevice {
    private String mNID;
    private NodeType mType;
    private String mStatus;
    private String mOffice;
    private String path;

    public NodeDevice(String NID, NodeType type, String status, String office, String path){
        this.mNID = NID;
        this.mType = type;
        this.mStatus = status;
        this.mOffice = office;
        this.path = path;
    }

    public String getmNID() {
        return mNID;
    }

    public void setmNID(String mNID) {
        this.mNID = mNID;
    }

    public NodeType getmType() {
        return mType;
    }

    public void setmType(NodeType mType) {
        this.mType = mType;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getmOffice() {
        return mOffice;
    }

    public void setmOffice(String mOffice) {
        this.mOffice = mOffice;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
