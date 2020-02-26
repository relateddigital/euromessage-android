package euromsg.com.euromobileandroid;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import euromsg.com.euromobileandroid.connection.ConnectionManager;
import euromsg.com.euromobileandroid.enums.MessageStatus;
import euromsg.com.euromobileandroid.model.Location;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.model.Retention;
import euromsg.com.euromobileandroid.model.Subscription;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.Utils;


public class EuroMobileManager {

    private static EuroMobileManager instance;

    private Subscription subscription = new Subscription();

    private boolean showPush = true;

    private EuroMobileManager(String applicationKey) {

        subscription.setFirstTime(1);
        subscription.setAppKey(applicationKey);
        subscription.setOs(Utils.osType());
        subscription.setOsVersion(Utils.osVersion());
        subscription.setSdkVersion(Constants.SDK_VERSION);
        subscription.setDeviceName(Utils.deviceName());
        subscription.setDeviceType(Utils.deviceType());
    }

    /**
     * Initiator method
     * <p>
     * Use to initiate Euromsg Service with application key and sender id
     *
     * @param applicationKey Application key from Euromsg. Euromsg will give you this key.
     */
    public static EuroMobileManager createInstance(String applicationKey, Context context) {
        if (instance == null) {
            instance = new EuroMobileManager(applicationKey);
        }
        EuroLogger.debugLog("SharedManager App Key : " + instance.subscription.getAppKey());
        Utils.saveSharedPrefString(context, Constants.APPLICATION_KEY, instance.subscription.getAppKey());

        return instance;
    }

    public static EuroMobileManager getInstance() {
        return instance;
    }

    /**
     * Retention service
     * <p>
     * Use to report when a GCM message is received. Only required when you perform a manuel GCM registration.
     *
     * @param pushId Message Id
     */
    public void reportReceived(String pushId) {

        if (pushId != null) {
            EuroLogger.debugLog("Report Received : " + pushId);

            Retention retention = new Retention();
            retention.setKey(subscription.getAppKey());
            retention.setPushId(pushId);
            retention.setStatus(MessageStatus.Received.toString());
            retention.setToken(subscription.getToken());
            ConnectionManager.getInstance().report(retention);
        } else {
            EuroLogger.debugLog("reportReceived : Push Id cannot be null!");
        }
    }

    /**
     * Register to FCM
     * <p>
     * Use to get a token from Firebase
     *
     * @param context Application context
     */
    public void registerToFCM(Context context) {
        FirebaseApp.initializeApp(context);
    }

    public void reportRead(String pushId) {

        if (pushId != null) {
            EuroLogger.debugLog("Report Read : " + pushId);
            Retention retention = new Retention();
            retention.setKey(subscription.getAppKey());
            retention.setPushId(pushId);
            retention.setStatus(MessageStatus.Read.toString());
            retention.setToken(subscription.getToken());
            ConnectionManager.getInstance().report(retention);
        } else {
            EuroLogger.debugLog("reportRead : Push Id cannot be null!");
        }
    }

    public void reportRead(Message message) {
        reportRead(message.getPushId());
    }

    public void reportReceived(Message message) throws Exception {
        reportReceived(message.getPushId());
    }

    public void subscribe(String token, Context context) {
        this.subscription.setToken(token);

        sync(context);
    }

    public void sync(Context context) {
        EuroLogger.debugLog("Sync started");
        if (this.subscription.isValid()) {
            saveSubscription(context);
            ConnectionManager.getInstance().subscribe(subscription);
        }
    }

    public void setAppVersion(String appVersion) {
        this.subscription.setAppVersion(appVersion);
    }

    public void setTwitterId(String twitterId, Context context) {
        setSubscriptionProperty(Constants.EURO_TWITTER_KEY, twitterId, context);
        Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setEmail(String email, Context context) {

        setSubscriptionProperty(Constants.EURO_EMAIL_KEY, email, context);

        Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setFacebook(String facebookId, Context context) {
        setSubscriptionProperty(Constants.EURO_FACEBOOK_KEY, facebookId, context);
        Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setLocation(double latitude, double longitude, Context context) {
        setSubscriptionProperty(Constants.EURO_LOCATION_KEY, new Location(latitude, longitude), context);
    }

    public void setEuroUserId(String userKey, Context context) {
        setSubscriptionProperty(Constants.EURO_USER_KEY, userKey, context);
        Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setPhoneNumber(String msisdn, Context context) {
        setSubscriptionProperty(Constants.EURO_MSISDN_KEY, msisdn, context);
        Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setUserProperty(String key, String value, Context context) {
        setSubscriptionProperty(key, value, context);
        Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void removeUserProperties(Context context) {
        this.subscription.removeAll();
        Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    private void saveSubscription(Context context) {

        subscription.setCarrier(Utils.carrier(context));
        subscription.setAppVersion(Utils.appVersion(context));
        subscription.setIdentifierForVendor(Utils.deviceUDID(context));
        subscription.setLocal(Utils.local(context));

        if (Utils.hasSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY)) {
            Subscription oldSubcription = new Gson().fromJson(Utils.getSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            subscription.addAll(oldSubcription.getExtra());
            subscription.setToken(oldSubcription.getToken());
            subscription.setAdvertisingIdentifier(oldSubcription.getAdvertisingIdentifier());
            subscription.setFirstTime(0);
        }
        try {
            EuroLogger.debugLog(this.subscription.toJson());
            Utils.saveSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();

        }
    }

    private void setSubscriptionProperty(String key, Object value, Context context) {

     if (Utils.hasSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY)) {
            this.subscription = new Gson().fromJson(Utils.getSharedPrefString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            this.subscription.add(key, value);

        } else {
            this.subscription.add(key, value);
        }
    }

    public void showPush(Boolean show) {
        this.showPush = show;
    }

    public boolean shouldShowPush() {
        return this.showPush;
    }

    public void setVisiUrl(String visiUrl) {
        ConnectionManager.getInstance().get(visiUrl);
    }
}
