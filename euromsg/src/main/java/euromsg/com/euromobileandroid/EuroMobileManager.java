package euromsg.com.euromobileandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

import euromsg.com.euromobileandroid.enums.EmailPermit;
import euromsg.com.euromobileandroid.enums.GsmPermit;
import euromsg.com.euromobileandroid.enums.PushPermit;
import euromsg.com.euromobileandroid.enums.PushType;
import euromsg.com.euromobileandroid.model.Element;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;

import com.google.firebase.BuildConfig;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import java.util.ArrayList;

import euromsg.com.euromobileandroid.connection.ApiClient;
import euromsg.com.euromobileandroid.connection.EuroApiService;
import euromsg.com.euromobileandroid.enums.BaseUrl;
import euromsg.com.euromobileandroid.enums.MessageStatus;
import euromsg.com.euromobileandroid.model.Location;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.model.Retention;
import euromsg.com.euromobileandroid.model.Subscription;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.SharedPreference;
import euromsg.com.euromobileandroid.utils.AppUtils;


public class EuroMobileManager {

    private static EuroMobileManager instance;

    private static EuroApiService apiInterface;

    private Subscription subscription = new Subscription();

    static Context mContext;

    private EuroMobileManager(String appAlias) {

        subscription.setFirstTime(1);
        subscription.setAppAlias(appAlias);
        subscription.setOs(AppUtils.osType());
        subscription.setOsVersion(AppUtils.osVersion());
        subscription.setSdkVersion(Constants.SDK_VERSION);
        subscription.setDeviceName(AppUtils.deviceName());
        subscription.setDeviceType(AppUtils.deviceType());
    }

    /**
     * Initiator method
     *
     * @param appAlias Application key from Euromsg. Euromsg will give you this key.
     */
    public static EuroMobileManager init(String appAlias, Context context) {

        if (instance == null) {
            instance = new EuroMobileManager(appAlias);
        }

        mContext = context;

        EuroLogger.debugLog("App Key : " + instance.subscription.getAppAlias());
        SharedPreference.saveString(context, Constants.APP_ALIAS, instance.subscription.getAppAlias());

        return instance;
    }

    public static EuroMobileManager getInstance() {
        return instance;
    }

    public void reportReceived(String pushId) {

        if (pushId != null) {
            EuroLogger.debugLog("Report Received : " + pushId);

            Retention retention = new Retention();
            retention.setKey(subscription.getAppAlias());
            retention.setPushId(pushId);
            retention.setStatus(MessageStatus.Received.toString());
            retention.setToken(subscription.getToken());

            apiInterface = ApiClient.getClient(BaseUrl.Retention).create(EuroApiService.class);
            Call<Void> call1 = apiInterface.report(retention);
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if (response.isSuccessful()) {
                        Log.d("ReportRecieved", "Success");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    call.cancel();
                }
            });

        } else {
            EuroLogger.debugLog("reportReceived : Push Id cannot be null!");
        }
    }

    public void registerToFCM(Context context) {
        FirebaseApp.initializeApp(context);
    }

    public void reportRead(Bundle bundle) {

          Message message = (Message) bundle.getSerializable("message");

        if ( message.getPushId() != null) {
            EuroLogger.debugLog("Report Read : " +  message.getPushId());
            Retention retention = new Retention();
            retention.setKey(subscription.getAppAlias());
            retention.setPushId(message.getPushId());
            retention.setStatus(MessageStatus.Read.toString());
            retention.setToken(subscription.getToken());

            apiInterface = ApiClient.getClient(BaseUrl.Retention).create(EuroApiService.class);

            Call<Void> call1 = apiInterface.report(retention);
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if (response.isSuccessful()) {
                        Log.d("reportRead", "Success");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    call.cancel();
                }
            });

        } else {
            EuroLogger.debugLog("reportRead : Push Id cannot be null!");
        }
    }

    public void reportReceived(Message message) throws Exception {
        reportReceived(message.getPushId());
    }

    public void subscribe(String token, Context context) {
        this.subscription.setToken(token);

        setDefaultPushPermit();
        sync(context);
    }

    private void setDefaultPushPermit() {
        this.subscription.add("pushPermit", "A");
    }

    public void sync(Context context) {
        EuroLogger.debugLog("Sync started");
        if (this.subscription.isValid()) {
            saveSubscription(context);

            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            apiInterface = ApiClient.getClient(BaseUrl.Subscription).create(EuroApiService.class);

            Call<Void> call1 = apiInterface.saveSubscription(subscription);

            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("isSuccesful", "OK");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    public void setAppVersion(String appVersion) {
        this.subscription.setAppVersion(appVersion);
    }

    public void setTwitterId(String twitterId, Context context) {
        setSubscriptionProperty(Constants.EURO_TWITTER_KEY, twitterId, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setEmail(String email, Context context) {

        setSubscriptionProperty(Constants.EURO_EMAIL_KEY, email, context);

        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setFacebook(String facebookId, Context context) {
        setSubscriptionProperty(Constants.EURO_FACEBOOK_KEY, facebookId, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setLocation(double latitude, double longitude, Context context) {
        setSubscriptionProperty(Constants.EURO_LOCATION_KEY, new Location(latitude, longitude), context);
    }

    public void setEuroUserId(String userKey, Context context) {
        setSubscriptionProperty(Constants.EURO_USER_KEY, userKey, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setPhoneNumber(String msisdn, Context context) {
        setSubscriptionProperty(Constants.EURO_MSISDN_KEY, msisdn, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setUserProperty(String key, String value, Context context) {
        setSubscriptionProperty(key, value, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void removeUserProperties(Context context) {
        this.subscription.removeAll();
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    private void saveSubscription(Context context) {

        subscription.setCarrier(AppUtils.carrier(context));
        subscription.setAppVersion(AppUtils.appVersion(context));
        subscription.setIdentifierForVendor(AppUtils.deviceUDID(context));
        subscription.setLocal(AppUtils.local(context));

        if (SharedPreference.hasString(context, Constants.EURO_SUBSCRIPTION_KEY)) {
            Subscription oldSubcription = new Gson().fromJson(SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            subscription.addAll(oldSubcription.getExtra());
            subscription.setToken(oldSubcription.getToken());
            subscription.setAdvertisingIdentifier(oldSubcription.getAdvertisingIdentifier());
            subscription.setFirstTime(0);
        }
        try {
            EuroLogger.debugLog(this.subscription.toJson());
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();

        }
    }

    private void setSubscriptionProperty(String key, Object value, Context context) {

        if (SharedPreference.hasString(context, Constants.EURO_SUBSCRIPTION_KEY)) {
            this.subscription = new Gson().fromJson(SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            this.subscription.add(key, value);

        } else {
            this.subscription.add(key, value);
        }
    }

    public Message getNotification(Intent intent) {

        Message message = (Message) intent.getSerializableExtra("message");

        return message;
    }

    public ArrayList<Element> getCarousels(Intent intent) {

        Message message = (Message) intent.getSerializableExtra("message");

        return message.getElements();
    }

    public void removeIntent(Intent intent) {
        intent.removeExtra("message");
    }

    private void setPushPermit(PushPermit pushPermit){
        this.subscription.add("pushPermit", pushPermit.name());
    }

    private void setGsmPermit(GsmPermit gsmPermit){
        this.subscription.add("gsmPermit", gsmPermit.name());
    }

    private void setGsmPermit(EmailPermit emailPermit){
        this.subscription.add("emailPermit", emailPermit.name());
    }

    public void setNotificationTransparentSmallIcon(int star_big_off) {
        SharedPreference.saveInt(mContext, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON, star_big_off);
    }

    public void setColor(String color) {
        SharedPreference.saveString(mContext, Constants.NOTIFICATION_COLOR, color);
    }
}