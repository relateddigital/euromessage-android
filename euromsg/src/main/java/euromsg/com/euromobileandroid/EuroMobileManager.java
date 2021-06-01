package euromsg.com.euromobileandroid;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
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
import java.util.ArrayList;
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
    private Subscription previousSubscription;
    private Subscription previousRegisterEmailSubscription;

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

        subscription.setFirstTime(0);
        subscription.setOs(AppUtils.osType());
        subscription.setOsVersion(AppUtils.osVersion());
        subscription.setSdkVersion(Constants.SDK_VERSION);
        subscription.setDeviceName(AppUtils.deviceName());
        subscription.setDeviceType(AppUtils.deviceType());
        subscription.setCarrier(AppUtils.carrier(context));
        subscription.setAppVersion(AppUtils.appVersion(context));
        subscription.setIdentifierForVendor(AppUtils.deviceUDID(context));
        subscription.setLocal(AppUtils.local(context));
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

    public void registerToFCM(final Context context) {
        FirebaseApp.initializeApp(context);
    }

    public void reportRead(Bundle bundle) {
        if (Build.VERSION.SDK_INT < Constants.UI_FEATURES_MIN_API) {
            Log.e("Euromessage", "Euromessage SDK requires min API level 21!");
            return;
        }

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

                if(RetentionApiClient.getClient() != null) {
                    apiInterface = RetentionApiClient.getClient().create(EuroApiService.class);
                }

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
        if (Build.VERSION.SDK_INT < Constants.UI_FEATURES_MIN_API) {
            Log.e("Euromessage", "Euromessage SDK requires min API level 21!");
            return;
        }

        EuroLogger.debugLog("Sync started");

        if (this.subscription.isValid() && !this.subscription.isEqual(previousSubscription)) {
            previousSubscription = new Subscription();
            previousSubscription.copyFrom(subscription);
            saveSubscription(context);
            setAppAlias(context);

            try {
                callNetworkSubscription(context);
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

        if(SubscriptionApiClient.getClient() != null) {
            apiInterface = SubscriptionApiClient.getClient().create(EuroApiService.class);
        }

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

    public void setAdvertisingIdentifier(String advertisingIdentifier, Context context) {
        this.subscription.setAdvertisingIdentifier(advertisingIdentifier);
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
        try {
            EuroLogger.debugLog(this.subscription.toJson());
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();

        }
    }

    private void setSubscriptionProperty(String key, Object value, Context context) {
        this.subscription.add(key, value);
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

    public void setNotificationLargeIcon(int largeIcon, Context context) {
        if (isResource(context, largeIcon)) {
            SharedPreference.saveInt(mContext, Constants.NOTIFICATION_LARGE_ICON, largeIcon);
        } else {
            Log.e("EM : Res Error", largeIcon + "");
        }
    }

    /**
     * This method is for setting if a large icon
     * will be used on the notification area.
     * @param willBeUsed : true to use it; false not to use it.
     *                     default value is true.
     */
    public void useNotificationLargeIcon(boolean willBeUsed) {
        SharedPreference.saveBoolean(mContext, Constants.NOTIFICATION_USE_LARGE_ICON, willBeUsed);
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

    public void removeNotificationLargeIcon() {
        SharedPreference.saveInt(mContext, Constants.NOTIFICATION_LARGE_ICON, 0);
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

    public void registerEmail(String email, EmailPermit emailPermit, Boolean isCommercial, Context context, final EuromessageCallback callback){
        if (Build.VERSION.SDK_INT < Constants.UI_FEATURES_MIN_API) {
            Log.e("Euromessage", "Euromessage SDK requires min API level 21!");
            return;
        }
        setEmail(email, context);
        setEmailPermit(emailPermit, context);
        Subscription registerEmailSubscription;
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
        if (registerEmailSubscription.isValid() && !registerEmailSubscription.isEqual(previousRegisterEmailSubscription)) {
            previousRegisterEmailSubscription = new Subscription();
            previousRegisterEmailSubscription.copyFrom(registerEmailSubscription);

            setThreadPolicy();
            if (SubscriptionApiClient.getClient() != null) {
                apiInterface = SubscriptionApiClient.getClient().create(EuroApiService.class);
            }
            Call<Void> call = apiInterface.saveSubscription(registerEmailSubscription);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Register Email Success");
                        if (callback != null) {
                            callback.success();
                        }
                    } else {
                        if (callback != null) {
                            callback.fail(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                    if (callback != null) {
                        callback.fail(t.getMessage());
                    }
                }
            });
        } else {
            Log.i(TAG, "The same email subscription with the previous one!");
        }
    }
}