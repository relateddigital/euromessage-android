package euromsg.com.euromobileandroid.model;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.LogUtils;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class Subscription extends BaseRequest implements Cloneable {

    @SerializedName("appVersion")
    private String appVersion;

    @SerializedName("appKey")
    private String appAlias;

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

    public boolean isValid(Context context) {
        boolean res1 = !(TextUtils.isEmpty(getToken()) && TextUtils.isEmpty(appAlias));
        boolean res2 = true;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateNow = dateFormat.format(Calendar.getInstance().getTime());
        String lastSubsTime = SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_DATE_KEY);
        if(lastSubsTime != null && !lastSubsTime.equals("")) {
            if(!AppUtils.isDateDifferenceGreaterThan(dateNow, lastSubsTime, 3)) {
                String lastSubStr = SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_NO_EMAIL_KEY);
                if(lastSubStr != null && !lastSubStr.isEmpty()) {
                    try {
                        Subscription lastSubscription = new Gson().fromJson(lastSubStr, Subscription.class);
                        if (isEqual(lastSubscription)) {
                            res2 = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_NO_EMAIL_KEY, "");
                    }
                }
            }
        }
        return (res1 & res2);
    }

    public boolean isValidWithEmail(Context context) {
        boolean res1 = !(TextUtils.isEmpty(getToken()) && TextUtils.isEmpty(appAlias));
        boolean res2 = true;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateNow = dateFormat.format(Calendar.getInstance().getTime());
        String lastSubsWithEmailTime = SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_DATE_WITH_EMAIL_KEY);
        if(lastSubsWithEmailTime != null && !lastSubsWithEmailTime.equals("")) {
            if(!AppUtils.isDateDifferenceGreaterThan(dateNow, lastSubsWithEmailTime, 3)) {
                String lastSubsWithEmailStr = SharedPreference.
                        getString(context, Constants.EURO_SUBSCRIPTION_WITH_EMAIL_KEY);
                if(lastSubsWithEmailStr != null && !lastSubsWithEmailStr.isEmpty()) {
                    try {
                        Subscription lastSubscriptionWithEmail = new Gson().fromJson(lastSubsWithEmailStr,
                                Subscription.class);
                        if (isEqual(lastSubscriptionWithEmail)) {
                            res2 = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_WITH_EMAIL_KEY, "");
                    }
                }
            }
        }
        return (res1 & res2);
    }

    public boolean isEqual(Subscription previousSubscription) {
        boolean result;
        if(previousSubscription == null) {
            result = false;
        } else {
            result = isStringEqual(this.appVersion, previousSubscription.getAppVersion()) &&
                    isStringEqual(this.appAlias, previousSubscription.getAppAlias()) &&
                    isStringEqual(this.os, previousSubscription.getOs()) &&
                    isStringEqual(this.osVersion, previousSubscription.getOsVersion()) &&
                    isStringEqual(this.deviceType, previousSubscription.getDeviceType()) &&
                    isStringEqual(this.deviceName, previousSubscription.getDeviceName()) &&
                    isStringEqual(this.carrier, previousSubscription.getCarrier()) &&
                    isStringEqual(this.local, previousSubscription.getLocal()) &&
                    isStringEqual(this.identifierForVendor, previousSubscription.getIdentifierForVendor()) &&
                    isStringEqual(this.advertisingIdentifier, previousSubscription.getAdvertisingIdentifier()) &&
                    isStringEqual(this.sdkVersion, previousSubscription.getSdkVersion()) &&
                    isStringEqual(this.getToken(), previousSubscription.getToken()) &&
                    this.firstTime == previousSubscription.getFirstTime() &&
                    isMapEqual(this.extra, previousSubscription.getExtra());
        }
        return result;
    }

    public void copyFrom(Subscription fromSubscription) {
        if(fromSubscription.getAppVersion() == null) {
            this.setAppVersion(null);
        } else {
            this.setAppVersion(fromSubscription.getAppVersion());
        }
        if(fromSubscription.getAppAlias() == null) {
            this.setAppAlias(null);
        } else {
            this.setAppAlias(fromSubscription.getAppAlias());
        }
        if(fromSubscription.getOs() == null) {
            this.setOs(null);
        } else {
            this.setOs(fromSubscription.getOs());
        }
        if(fromSubscription.getOsVersion() == null) {
            this.setOsVersion(null);
        } else {
            this.setOsVersion(fromSubscription.getOsVersion());
        }
        if(fromSubscription.getDeviceType() == null) {
            this.setDeviceType(null);
        } else {
            this.setDeviceType(fromSubscription.getDeviceType());
        }
        if(fromSubscription.getDeviceName() == null) {
            this.setDeviceName(null);
        } else {
            this.setDeviceName(fromSubscription.getDeviceName());
        }
        if(fromSubscription.getCarrier() == null) {
            this.setCarrier(null);
        } else {
            this.setCarrier(fromSubscription.getCarrier());
        }
        if(fromSubscription.getLocal() == null) {
            this.setLocal(null);
        } else {
            this.setLocal(fromSubscription.getLocal());
        }
        if(fromSubscription.getIdentifierForVendor() == null) {
            this.setIdentifierForVendor(null);
        } else {
            this.setIdentifierForVendor(fromSubscription.getIdentifierForVendor());
        }
        if(fromSubscription.getAdvertisingIdentifier() == null) {
            this.setAdvertisingIdentifier(null);
        } else {
            this.setAdvertisingIdentifier(fromSubscription.getAdvertisingIdentifier());
        }
        if(fromSubscription.getSdkVersion() == null) {
            this.setSdkVersion(null);
        } else {
            this.setSdkVersion(fromSubscription.getSdkVersion());
        }
        this.setFirstTime(fromSubscription.getFirstTime());
        if(fromSubscription.getToken() == null) {
            this.setToken(null);
        } else {
            this.setToken(fromSubscription.getToken());
        }
        this.extra = new HashMap<>();
        for(int i=0 ; i<fromSubscription.getExtra().keySet().toArray().length; i++) {
            String key = (String) fromSubscription.getExtra().keySet().toArray()[i];
            if(fromSubscription.getExtra().get(key) == null) {
                this.extra.put(key, null);
            } else {
                this.extra.put(key, fromSubscription.getExtra().get(key));
            }
        }
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAppAlias() {
        return appAlias;
    }

    public void setAppAlias(String appAlias) {
        this.appAlias = appAlias;
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

    private boolean isStringEqual(String first, String second) {
        boolean result;
        if(first == null || second == null) {
            result = first == null && second == null;
        } else {
            result = first.equals(second);
        }
        return result;
    }

    private boolean isMapEqual(Map<String, Object> first, Map<String, Object> second) {
        boolean result = true;
        if(first.size() != second.size()) {
            result = false;
        } else {
            for(int i=0 ; i<first.keySet().toArray().length; i++) {
                String key = (String) first.keySet().toArray()[i];
                if(key.equals(Constants.EURO_CONSENT_TIME_KEY)) {
                    continue;
                }
                if(!second.containsKey(key)) {
                    result = false;
                    break;
                } else {
                    String value1 = (String) first.get(key);
                    String value2 = (String) second.get(key);
                    if(value1 == null || value2 == null) {
                        if(!(value1 == null && value2 == null)) {
                            result = false;
                            break;
                        }
                    } else {
                        if(!value1.equals(value2)) {
                            result = false;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        Subscription s = (Subscription)super.clone();
        s.extra  = new HashMap<>();
        s.extra.putAll(this.extra);

        //s.extra  = (Map<String, Object>) ((HashMap<String, Object>) this.extra).clone();
        return s;
    }
}