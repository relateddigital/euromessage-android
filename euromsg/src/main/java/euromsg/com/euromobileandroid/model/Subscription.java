package euromsg.com.euromobileandroid.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import euromsg.com.euromobileandroid.utils.EuroLogger;

public class Subscription extends BaseRequest {

    @SerializedName("appVersion")
    private String appVersion;

    @SerializedName("appKey")
    private String appKey;

    @SerializedName("os")
    private String os;

    @SerializedName("osVersion")
    private String osVersion;

    @SerializedName("deviceType")
    private String deviceType;

    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("carrier")
    private String carrier;

    @SerializedName("local")
    private String local;

    @SerializedName("identifierForVendor")
    private String identifierForVendor;

    @SerializedName("advertisingIdentifier")
    private String advertisingIdentifier;

    @SerializedName("sdkVersion")
    private String sdkVersion;

    @SerializedName("firstTime")
    private int firstTime;

    @SerializedName("extra")
    private Map<String, Object> extra = new HashMap<>();

    public void add(String key, Object value) {
        extra.put(key, value);
    }

    public void addAll(Map<String, Object> extras) {
        extra.putAll(extras);
    }

    public void removeAll() {
        this.extra.clear();
    }

    public boolean isValid() {
        EuroLogger.debugLog("Token : " + getToken() + " App Key : " + this.appKey);
        return !(TextUtils.isEmpty(getToken()) && TextUtils.isEmpty(appKey));
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getIdentifierForVendor() {
        return identifierForVendor;
    }

    public void setIdentifierForVendor(String identifierForVendor) {
        this.identifierForVendor = identifierForVendor;
    }

    public String getAdvertisingIdentifier() {
        return advertisingIdentifier;
    }

    public void setAdvertisingIdentifier(String advertisingIdentifier) {
        this.advertisingIdentifier = advertisingIdentifier;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public int getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(int firstTime) {
        this.firstTime = firstTime;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}