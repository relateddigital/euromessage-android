package euromsg.com.euromobileandroid.model;

import com.google.gson.annotations.SerializedName;

public class Retention extends BaseRequest {

    @SerializedName("key")
    private String key;

    @SerializedName("pushId")
    private String pushId;

    @SerializedName("status")
    private String status;

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
}