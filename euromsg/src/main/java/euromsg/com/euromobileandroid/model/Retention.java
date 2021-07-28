package euromsg.com.euromobileandroid.model;

import com.google.gson.annotations.SerializedName;

public class Retention extends BaseRequest {

    @SerializedName("key")
    private String key;

    @SerializedName("pushId")
    private String pushId;

    @SerializedName("status")
    private String status;

    @SerializedName("deliver")
    private int deliver;

    @SerializedName("isMobile")
    private int isMobile = 1;

    @SerializedName("actionBtn")
    private int actionBtn = 0;

    @SerializedName("emPushSp")
    private String emPushSp;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDeliver() {
        return deliver;
    }

    public void setDeliver(int deliver) {
        this.deliver = deliver;
    }

    public int getIsMobile() {
        return isMobile;
    }

    public void setIsMobile(int isMobile) {
        this.isMobile = isMobile;
    }

    public int getActionBtn() {
        return actionBtn;
    }

    public void setActionBtn(int actionBtn) {
        this.actionBtn = actionBtn;
    }

    public String getEmPushSp() {
        return emPushSp;
    }

    public void setEmPushSp(String emPushSp) {
        this.emPushSp = emPushSp;
    }
}