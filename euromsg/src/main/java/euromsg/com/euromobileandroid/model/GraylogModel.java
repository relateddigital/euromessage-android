package euromsg.com.euromobileandroid.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GraylogModel implements Serializable {

    @SerializedName("logLevel")
    private String logLevel = "";

    @SerializedName("logMessage")
    private String logMessage = "";

    @SerializedName("logPlace")
    private String logPlace = "";

    @SerializedName("googleAppAlias")
    private String googleAppAlias = "";

    @SerializedName("huaweiAppAlias")
    private String huaweiAppAlias = "";

    @SerializedName("iosAppAlias")
    private String iosAppAlias = "";

    @SerializedName("token")
    private String token = "";

    @SerializedName("appVersion")
    private String appVersion = "";

    @SerializedName("sdkVersion")
    private String sdkVersion = "";

    @SerializedName("osType")
    private String osType = "";

    @SerializedName("osVersion")
    private String osVersion = "";

    @SerializedName("deviceName")
    private String deviceName = "";

    @SerializedName("userAgent")
    private String userAgent = "";

    @SerializedName("identifierForVendor")
    private String identifierForVendor = "";

    @SerializedName("extra")
    private Map<String, Object> extra = new HashMap<>();

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public void setLogPlace(String logPlace) {
        this.logPlace = logPlace;
    }

    public void setGoogleAppAlias(String googleAppAlias) {
        this.googleAppAlias = googleAppAlias;
    }

    public void setHuaweiAppAlias(String huaweiAppAlias) {
        this.huaweiAppAlias = huaweiAppAlias;
    }

    public void setIosAppAlias(String iosAppAlias) {
        this.iosAppAlias = iosAppAlias;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setIdentifierForVendor(String identifierForVendor) {
        this.identifierForVendor = identifierForVendor;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra.clear();
        this.extra.putAll(extra);
    }

    public String getLogLevel(){
        return this.logLevel;
    }

    public String getLogMessage(){
        return this.logMessage;
    }

    public String getLogPlace(){
        return this.logPlace;
    }

    public String getGoogleAppAlias(){
        return this.googleAppAlias;
    }

    public String getHuaweiAppAlias(){
        return this.huaweiAppAlias;
    }

    public String getIosAppAlias(){
        return this.iosAppAlias;
    }

    public String getToken(){
        return this.token;
    }

    public String getAppVersion(){
        return this.appVersion;
    }

    public String getSdkVersion(){
        return this.sdkVersion;
    }

    public String getOsType(){
        return this.osType;
    }

    public String getOsVersion(){
        return this.osVersion;
    }

    public String getDeviceName(){
        return this.deviceName;
    }

    public String getUserAgent(){
        return this.userAgent;
    }

    public String getIdentifierForVendor(){
        return this.identifierForVendor;
    }

    public Map<String, Object> getExtra(){
        return this.extra;
    }
}
