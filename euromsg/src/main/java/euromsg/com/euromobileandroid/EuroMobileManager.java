package euromsg.com.euromobileandroid;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.BuildConfig;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euromsg.com.euromobileandroid.connection.EuroApiService;
import euromsg.com.euromobileandroid.connection.RetentionApiClient;
import euromsg.com.euromobileandroid.connection.SubscriptionApiClient;
import euromsg.com.euromobileandroid.enums.EmailPermit;
import euromsg.com.euromobileandroid.enums.GsmPermit;
import euromsg.com.euromobileandroid.enums.MessageStatus;
import euromsg.com.euromobileandroid.enums.PushPermit;
import euromsg.com.euromobileandroid.model.Element;
import euromsg.com.euromobileandroid.model.EuromessageCallback;
import euromsg.com.euromobileandroid.model.Location;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.model.Retention;
import euromsg.com.euromobileandroid.model.Subscription;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.SharedPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EuroMobileManager {

    private static EuroMobileManager instance;

    private static EuroApiService apiInterface;

    public static String huaweiAppAlias;
    public static String firebaseAppAlias;

    private Subscription subscription;

    private static Context mContext;

    static String TAG = "EuroMobileManager";

    private EuroMobileManager(Context context, String googleAppAlias, String huaweiAppAlias) {

        subscription = new Gson().fromJson(SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
        if(subscription == null) {
            subscription = new Subscription();
        }

        if (checkPlayService(context)) {
            subscription.setAppAlias(googleAppAlias);
        } else {
            subscription.setAppAlias(huaweiAppAlias);
        }

        subscription.setFirstTime(1);
        subscription.setOs(AppUtils.osType());
        subscription.setOsVersion(AppUtils.osVersion());
        subscription.setSdkVersion(Constants.SDK_VERSION);
        subscription.setDeviceName(AppUtils.deviceName());
        subscription.setDeviceType(AppUtils.deviceType());
    }

    public static EuroMobileManager init(String googleAppAlias, String huwaeiAppAlias, Context context) {

        if (instance == null) {
            instance = new EuroMobileManager(context, googleAppAlias, huwaeiAppAlias);
        }

        huaweiAppAlias = huwaeiAppAlias;
        firebaseAppAlias = googleAppAlias;

        mContext = context;

        if (checkPlayService(context)) {
            SharedPreference.saveString(context, Constants.GOOGLE_APP_ALIAS, instance.subscription.getAppAlias());
        } else {
            SharedPreference.saveString(context, Constants.HUAWEI_APP_ALIAS, instance.subscription.getAppAlias());
        }

        EuroLogger.debugLog("App Key : " + instance.subscription.getAppAlias());

        return instance;
    }

    public static EuroMobileManager getInstance() {
        return instance;
    }

    public void reportReceived(String pushId) {

        if (pushId != null) {
            EuroLogger.debugLog("Report Received : " + pushId);

            Retention retention = new Retention();
            if (checkPlayService(mContext)) {
                retention.setKey(firebaseAppAlias);
            } else {
                retention.setKey(huaweiAppAlias);
            }

            retention.setPushId(pushId);
            retention.setStatus(MessageStatus.Received.toString());
            retention.setToken(subscription.getToken());

            apiInterface = RetentionApiClient.getClient().create(EuroApiService.class);
            Call<Void> call1 = apiInterface.report(retention);
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                    if (response.isSuccessful()) {
                        Log.d("ReportRecieved", "Success");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });

        } else {
            EuroLogger.debugLog("reportReceived : Push Id cannot be null!");
        }
    }

    public void registerToFCM(final Context context) {
        FirebaseApp.initializeApp(context);
    }

    public void reportRead(Bundle bundle) {

        Message message = (Message) bundle.getSerializable("message");

        if (message != null) {
            if (message.getPushId() != null) {
                EuroLogger.debugLog("Report Read : " + message.getPushId());
                Retention retention = new Retention();

                if (checkPlayService(mContext)) {
                    retention.setKey(firebaseAppAlias);
                } else {
                    retention.setKey(huaweiAppAlias);
                }

                retention.setPushId(message.getPushId());
                retention.setStatus(MessageStatus.Read.toString());
                retention.setToken(subscription.getToken());

                apiInterface = RetentionApiClient.getClient().create(EuroApiService.class);

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
    }

    public void reportReceived(Message message) throws Exception {
        reportReceived(message.getPushId());
    }

    public void subscribe(String token, Context context) {
        this.subscription.setToken(token);

        setDefaultPushPermit(context);

        sync(context);
    }

    private void setDefaultPushPermit(Context context) {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            setPushPermit(PushPermit.ACTIVE, context);
        } else {
            setPushPermit(PushPermit.PASSIVE, context);
        }
    }

    public void sync(Context context) {

        EuroLogger.debugLog("Sync started");

        if (this.subscription.isValid()) {
            saveSubscription(context);
            setAppAlias(context);

            try {
                if (shouldSendSubscription(context)) {
                    callNetworkSubscription(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callNetworkSubscription(context);
            }

        } else {
            Log.i(TAG, "Not Valid Subs");
        }
    }

    private void setAppAlias(Context context) {
        if (checkPlayService(context)) {
            subscription.setAppAlias(firebaseAppAlias);
        } else {
            subscription.setAppAlias(huaweiAppAlias);
        }
    }

    private void callNetworkSubscription(final Context context) {

        setThreadPolicy();

        apiInterface = SubscriptionApiClient.getClient().create(EuroApiService.class);

        Call<Void> call1 = apiInterface.saveSubscription(subscription);


        call1.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    SharedPreference.saveString(context, Constants.ALREADY_SENT_SUBSCRIPTION_JSON, subscription.toJson());
                    SharedPreference.saveString(context, Constants.LAST_SUBSCRIPTION_TIME, AppUtils.getCurrentDateString());
                    Log.i(TAG, "Sync Success");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                call.cancel();
                t.printStackTrace();
            }
        });
    }

    private boolean isSubscriptionAllReadySent(Context context) {

        boolean value = false;
        if (SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_KEY).equals(SharedPreference.getString(context, Constants.ALREADY_SENT_SUBSCRIPTION_JSON))) {
            value = true;
        }

        return value;
    }

    private void setThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    public void setPushPermit(PushPermit pushPermit, Context context) {
        setSubscriptionProperty("pushPermit", pushPermit.name, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setGsmPermit(GsmPermit gsmPermit, Context context) {
        setSubscriptionProperty("gsmPermit", gsmPermit.name, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setEmailPermit(EmailPermit emailPermit, Context context) {
        setSubscriptionProperty("emailPermit", emailPermit.name, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
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
            Subscription oldSubscription = new Gson().fromJson(SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            subscription.addAll(oldSubscription.getExtra());
            subscription.setAdvertisingIdentifier(oldSubscription.getAdvertisingIdentifier());
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
        this.subscription.add(key, value);
        /*
        if (SharedPreference.hasString(context, Constants.EURO_SUBSCRIPTION_KEY)) {
            this.subscription = new Gson().fromJson(SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_KEY), Subscription.class);
            this.subscription.add(key, value);

        } else {
            this.subscription.add(key, value);
        }
        */
    }

    public Message getNotification(Intent intent) {

        Message message = (Message) intent.getSerializableExtra("message");

        return message;
    }

    public ArrayList<Element> getCarousels(Intent intent) {

        Message message = (Message) intent.getSerializableExtra("message");

        return message.getElements();
    }

    public void removeIntentExtra(Intent intent) {
        intent.removeExtra("message");
    }

    public void setNotificationTransparentSmallIcon(int transparentSmallIcon, Context context) {
        if (isResource(context, transparentSmallIcon)) {
            SharedPreference.saveInt(mContext, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON, transparentSmallIcon);
        } else {
            Log.e("EM : Res Error", transparentSmallIcon + "");
        }
    }

    public void setNotificationColor(String color) {

        if (isValidColor(color)) {
            SharedPreference.saveString(mContext, Constants.NOTIFICATION_COLOR, color);
        } else {
            Log.e("EM : Color Error", color);
        }
    }

    private static boolean isResource(Context context, int resId) {
        if (context != null) {
            try {
                return context.getResources().getResourceName(resId) != null;
            } catch (Resources.NotFoundException ignore) {
            }
        }
        return false;
    }

    private boolean isValidColor(String color) {
        Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
        Matcher m = colorPattern.matcher(color);
        return m.matches();
    }

    public void removeNotificationTransparentSmallIcon() {
        SharedPreference.saveInt(mContext, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON, 0);
    }

    public void removeNotificationColor() {
        SharedPreference.saveString(mContext, Constants.NOTIFICATION_COLOR, "");
    }

    public void setChannelName(String channelName, Context context) {
        SharedPreference.saveString(context, Constants.CHANNEL_NAME, channelName);
    }

    public void removeChannelName(Context context) {
        SharedPreference.saveString(context, Constants.CHANNEL_NAME, "");
    }

    public void showBuilderNumber(boolean isShown, Context context) {
        if (isShown) {
            SharedPreference.saveInt(context, Constants.BADGE, Constants.ACTIVE);
        } else {
            SharedPreference.saveInt(context, Constants.BADGE, Constants.PASSIVE);
        }
    }

    public void setPushIntent(String intentStr, Context context) {
        SharedPreference.saveString(context, Constants.INTENT_NAME, intentStr);
    }

    public void removePushIntent(Context context) {
        SharedPreference.saveString(context, Constants.INTENT_NAME, "");
    }

    public static boolean checkPlayService(Context context) {
        boolean result = true;

        int isGoogleEnabled = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        switch (isGoogleEnabled) {
            case ConnectionResult.API_UNAVAILABLE:
                Log.e(TAG, "Google API Unavailable");
                result = false;
                //API is not available
                break;

            case ConnectionResult.NETWORK_ERROR:
                Log.e(TAG, "Google Network Error");
                result = false;

                //Network error while connection
                break;

            case ConnectionResult.RESTRICTED_PROFILE:
                Log.e(TAG, "Google Restricted");
                result = false;

                //Profile is restricted by google so can not be used for play services
                break;

            case ConnectionResult.SERVICE_MISSING:
                //service is missing
                result = false;

                Log.e(TAG, "Google Service is missing");

                break;

            case ConnectionResult.SIGN_IN_REQUIRED:
                //service available but user not signed in
                Log.e(TAG, "Google Sign in req");
                result = false;

                break;
            case ConnectionResult.SERVICE_INVALID:
                Log.e(TAG, "Google Services invalid");
                result = false;

                //  The version of the Google Play services installed on this device is not authentic
                break;
            case ConnectionResult.SUCCESS:
                result = true;

                Log.i(TAG, "Google Service is enable");

                break;
        }

        return result;
    }

    public boolean shouldSendSubscription(Context context) throws ParseException {
        boolean value ;

        if (isSubscriptionAllReadySent(context)) {

            if (!SharedPreference.getString(context, Constants.LAST_SUBSCRIPTION_TIME).equals("")) {

                Date lastSubsDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(SharedPreference.getString(context, Constants.LAST_SUBSCRIPTION_TIME));
                Date today = new Date();
                long diff = lastSubsDate.getTime()- today.getTime();

                int min = (int) (diff / (1000 * 60));
                int seconds = (int) (- diff / (1000));
                int remaining = 1200 - seconds;

                if (seconds > 1200 || seconds<-1200) {
                    value = true;

                } else {
                    Log.i(TAG, "Have to wait " + remaining  +" seconds to send the same subscription" );
                    value = false;
                }
            } else {
                value = true;
                Log.i(TAG, "First subscription request has been received");

            }
        } else {

            value = true;
        }

        return value;
    }

    public void registerEmail(String email, EmailPermit emailPermit, Boolean isCommercial, Context context, final EuromessageCallback callback){
        setEmail(email, context);
        setEmailPermit(emailPermit, context);
        Subscription registerEmailSubscription = null;
        try {
            registerEmailSubscription = (Subscription) this.subscription.clone();
            registerEmailSubscription.add(Constants.EURO_CONSENT_SOURCE_KEY, Constants.EURO_CONSENT_SOURCE_VALUE);
            registerEmailSubscription.add(Constants.EURO_RECIPIENT_TYPE_KEY
                    , isCommercial ? Constants.EURO_RECIPIENT_TYPE_TACIR : Constants.EURO_RECIPIENT_TYPE_BIREYSEL);
            registerEmailSubscription.add(Constants.EURO_CONSENT_TIME_KEY, AppUtils.getCurrentTurkeyDateString());
        } catch (Exception ex) {
            if(callback != null) {
                callback.fail(ex.getMessage());
            }
            return;
        }
        setThreadPolicy();
        apiInterface = SubscriptionApiClient.getClient().create(EuroApiService.class);
        Call<Void> call = apiInterface.saveSubscription(registerEmailSubscription);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Register Email Success");
                    if(callback != null) {
                        callback.success();
                    }
                } else {
                    if(callback != null) {
                        callback.fail(response.message());
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                call.cancel();
                t.printStackTrace();
                if(callback != null) {
                    callback.fail(t.getMessage());
                }
            }
        });
    }
}