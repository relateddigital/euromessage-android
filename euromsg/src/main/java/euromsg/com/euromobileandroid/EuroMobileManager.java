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
    public static EuroMobileManager sharedManager(String applicationKey) {
        if (instance == null) {
            instance = new EuroMobileManager(applicationKey);
        }
        EuroLogger.debugLog("SharedManager App Key : " + instance.subscription.getAppKey());
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
     * Register to GCM
     * <p>
     * Use to get a token from Firebase
     *
     * @param context Application context
     */
    public void registerToFCM(Context context) {
        FirebaseApp.initializeApp(context);
    }

    /**
     * Retention service
     * <p>
     * Use to report when a GCM message is read.
     *
     * @param pushId Message Id
     */
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

    /**
     * Retention service
     * <p>
     * Use to report when a GCM message is read.
     *
     * @param message Message from GCM
     */
    public void reportRead(Message message) {
        reportRead(message.getPushId());
    }

    /**
     * Retention service
     * <p>
     * Use to report when a GCM message is received. Only required when you perform a manuel GCM registration.
     *
     * @param message Message from GCM
     */
    public void reportReceived(Message message) {
        reportReceived(message.getPushId());
    }

    /**
     * Subscribe User
     * <p>
     * Use to register a user to Euromsg. Only required when you perform a manuel GCM registration.
     *
     * @param token   GCM Token
     * @param context Application context
     */
    public void subscribe(String token, Context context) {
        this.subscription.setToken(token);

        sync(context);
    }

    /**
     * Sync user information with Euromsg
     * <p>
     * Use to send the latest information to Euromsg. If you set any property or perform a logout, you are advised to call this method.
     *
     * @param context Application context
     */
    public void sync(Context context) {
        EuroLogger.debugLog("Sync started");
        if (this.subscription.isValid()) {
            saveSubscription(context);
            ConnectionManager.getInstance().subscribe(subscription);
        }
    }

    /**
     * Set Application Version
     * <p>
     * Use to set application version
     *
     * @param appVersion Application version
     */
    public void setAppVersion(String appVersion) {
        this.subscription.setAppVersion(appVersion);
    }

    /**
     * Set User Twitter Id
     * <p>
     * Use to set twitter id to a user.
     *
     * @param twitterId Twitter Id
     * @param context   context
     */
    public void setTwitterId(String twitterId, Context context) {
        setSubscriptionProperty(Constants.EURO_TWITTER_KEY, twitterId, context);
        Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    /**
     * Set User Email
     * <p>
     * Use to set email to a user.
     *
     * @param email   Email of a user
     * @param context context
     */
    public void setEmail(String email, Context context) {

        setSubscriptionProperty(Constants.EURO_EMAIL_KEY, email, context);

        Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    /**
     * Set User Facebook Id
     * <p>
     * Use to set facebook id to a user.
     *
     * @param facebookId Facebook Id
     * @param context    context
     */
    public void setFacebook(String facebookId, Context context) {
        setSubscriptionProperty(Constants.EURO_FACEBOOK_KEY, facebookId, context);
        Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    /**
     * Set User Location
     * <p>
     * Use to set last known location to a user.
     *
     * @param latitude  Latitude
     * @param longitude Longitude
     */
    public void setLocation(double latitude, double longitude, Context context) {
        setSubscriptionProperty(Constants.EURO_LOCATION_KEY, new Location(latitude, longitude), context);
    }

    /**
     * Set User Euromsg Id
     * <p>
     * Use to set Euromsg id to a user.
     *
     * @param userKey user id
     * @param context context
     */
    public void setEuroUserId(String userKey, Context context) {
        setSubscriptionProperty(Constants.EURO_USER_KEY, userKey, context);
        Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    /**
     * Set User Phone
     * <p>
     * Use to set phone number to a user.
     *
     * @param msisdn  phone number
     * @param context context
     */
    public void setPhoneNumber(String msisdn, Context context) {
        setSubscriptionProperty(Constants.EURO_MSISDN_KEY, msisdn, context);
        Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    /**
     * Set User Property
     * <p>
     * Use to set a custom property to a user.
     *
     * @param key     key for the property
     * @param value   value for the property
     * @param context context
     */
    public void setUserProperty(String key, String value, Context context) {
        setSubscriptionProperty(key, value, context);
        Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    /**
     * Remove User Properties
     * <p>
     * If you have set user properties before, you can remove them here. Preferred when the user logs out from the app. Use sync afterwards.
     *
     * @param context context
     */
    public void removeUserProperties(Context context) {
        this.subscription.removeAll();
        Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }


    private void saveSubscription(Context context) {
        subscription.setCarrier(Utils.carrier(context));
        subscription.setAppVersion(Utils.appVersion(context));
        subscription.setIdentifierForVendor(Utils.deviceUDID(context));
        subscription.setLocal(Utils.local(context));
        if (Utils.hasPrefString(context, Constants.EURO_SUBSCRIPTION_KEY)) {
            Subscription oldSubcription = new Gson().fromJson(Utils.getPrefString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            subscription.addAll(oldSubcription.getExtra());
            subscription.setToken(oldSubcription.getToken());
            subscription.setAdvertisingIdentifier(oldSubcription.getAdvertisingIdentifier());
            subscription.setFirstTime(0);
        }
        try {
            EuroLogger.debugLog(this.subscription.toJson());
            Utils.savePrefString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();

        }
    }

    private void setSubscriptionProperty(String key, Object value, Context context) {

        if (Utils.hasPrefString(context, Constants.EURO_SUBSCRIPTION_KEY)) {
            this.subscription = new Gson().fromJson(Utils.getPrefString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            this.subscription.add(key, value);

        } else {
            this.subscription.add(key, value);
        }
    }

    /**
     * If you prefer the SDK to show a notification, use this property
     *
     * @param show Whether the SDK should show notification
     */
    public void showPush(Boolean show) {
        this.showPush = show;
    }

    public boolean shouldShowPush() {
        return this.showPush;
    }

    /**
     * Visilabs Integration
     * <p>
     * If you have Visilabs within your application, use this method to inform Visilabs.
     *
     * @param visiUrl The new token.
     */
    public void setVisiUrl(String visiUrl) {
        ConnectionManager.getInstance().get(visiUrl);
    }
}
