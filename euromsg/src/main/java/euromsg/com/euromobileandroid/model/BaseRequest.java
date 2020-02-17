package euromsg.com.euromobileandroid.model;

import com.google.gson.annotations.SerializedName;

public abstract class BaseRequest {

    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
