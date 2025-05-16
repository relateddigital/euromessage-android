package euromsg.com.euromobileandroid;

import static euromsg.com.euromobileandroid.Constants.LOG_TAG;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.BuildConfig;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euromsg.com.euromobileandroid.callback.PushMessageInterface;
import euromsg.com.euromobileandroid.callback.PushNotificationPermissionInterface;
import euromsg.com.euromobileandroid.connection.EuroApiService;
import euromsg.com.euromobileandroid.connection.RetentionApiClient;
import euromsg.com.euromobileandroid.connection.SubscriptionApiClient;
import euromsg.com.euromobileandroid.enums.EmailPermit;
import euromsg.com.euromobileandroid.enums.GsmPermit;
import euromsg.com.euromobileandroid.enums.MessageStatus;
import euromsg.com.euromobileandroid.enums.PushPermit;
import euromsg.com.euromobileandroid.enums.RDNotificationPriority;
import euromsg.com.euromobileandroid.model.Element;
import euromsg.com.euromobileandroid.model.EuromessageCallback;
import euromsg.com.euromobileandroid.model.GraylogModel;
import euromsg.com.euromobileandroid.model.Location;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.model.Retention;
import euromsg.com.euromobileandroid.model.Subscription;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.LogUtils;
import euromsg.com.euromobileandroid.utils.NotificationPermissionActivity;
import euromsg.com.euromobileandroid.utils.PayloadUtils;
import euromsg.com.euromobileandroid.utils.RetryCounterManager;
import euromsg.com.euromobileandroid.utils.SharedPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EuroMobileManager {

    private static EuroMobileManager instance;

    private static EuroApiService retentionApiInterface;
    private static EuroApiService subscriptionApiInterface;
    private static String mUserAgent;

    public Subscription subscription;
    private Subscription previousSubscription;
    private Subscription previousRegisterEmailSubscription;
    private String latestDeliverPushId = "";
    private String latestOpenPushId = "";

    private static Context mContext;

    private GraylogModel graylogModel = null;

    static String TAG = "EuroMobileManager";

    private EuroMobileManager(Context context, String googleAppAlias, String huaweiAppAlias) {

        try {
            String subsStr = SharedPreference.getString(context, Constants.EURO_SUBSCRIPTION_KEY);
            if(!subsStr.isEmpty()) {
                subscription = new Gson().fromJson(subsStr, Subscription.class);
            }
        } catch (Exception e) {
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, "");
            subscription = null;
        }

        if(subscription == null) {
            subscription = new Subscription();
        }

        if (checkPlayService(context)) {
            subscription.setAppAlias(googleAppAlias);
        } else {
            subscription.setAppAlias(huaweiAppAlias);
        }

        if(RetentionApiClient.getClient() != null) {
            retentionApiInterface = RetentionApiClient.getClient().create(EuroApiService.class);
        }

        if(SubscriptionApiClient.getClient() != null) {
            subscriptionApiInterface = SubscriptionApiClient.getClient().create(EuroApiService.class);
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

        mUserAgent = System.getProperty("http.agent");

        fillGraylogModel();
    }

    public static EuroMobileManager init(String googleAppAlias, String huwaeiAppAlias, Context context) {

        mContext = context;

        if (instance == null) {
            instance = new EuroMobileManager(context, googleAppAlias, huwaeiAppAlias);
        }

        if (checkPlayService(context)) {
            SharedPreference.saveString(context, Constants.GOOGLE_APP_ALIAS, instance.subscription.getAppAlias());
        } else {
            SharedPreference.saveString(context, Constants.HUAWEI_APP_ALIAS, instance.subscription.getAppAlias());
        }

        EuroLogger.debugLog("App Key : " + instance.subscription.getAppAlias());

        return instance;
    }

    public static EuroMobileManager init(Context context) {

        String googleAlias = SharedPreference.getString(context, Constants.GOOGLE_APP_ALIAS);
        String huaweiAlias = SharedPreference.getString(context, Constants.HUAWEI_APP_ALIAS);

        if(googleAlias.isEmpty() && huaweiAlias.isEmpty()) {
            Log.e(TAG, "Could not create EuroMobileManager instance!!!");
            return null;
        }

        if (instance == null) {
            instance = new EuroMobileManager(context, googleAlias, huaweiAlias);
        }

        mContext = context;

        EuroLogger.debugLog("App Key : " + instance.subscription.getAppAlias());

        return instance;
    }

    public static EuroMobileManager getInstance() {
        return instance;
    }

    public void registerToFCM(final Context context) {
        FirebaseApp.initializeApp(context);
    }

    public void reportReceived(String pushId, String emPushSp, Boolean isSilent) {

        if (pushId != null && !pushId.isEmpty() && !pushId.equals(latestDeliverPushId)) {
            latestDeliverPushId = pushId;
            EuroLogger.debugLog("Report Received : " + pushId);

            Retention retention = new Retention();
            retention.setKey(subscription.getAppAlias());

            retention.setPushId(pushId);
            if(isSilent) {
                retention.setStatus(MessageStatus.Silent.toString());
            } else {
                retention.setStatus(MessageStatus.Received.toString());
            }
            retention.setToken(SharedPreference.getString(mContext, Constants.TOKEN_KEY));
            retention.setActionBtn(0);
            retention.setDeliver(1);
            retention.setIsMobile(1);
            if(emPushSp != null) {
                retention.setEmPushSp(emPushSp);
            }

            if(RetentionApiClient.getClient() != null) {
                retentionApiInterface = RetentionApiClient.getClient().create(EuroApiService.class);
                reportReceivedRequest(retention, RetryCounterManager.getCounterId());
            } else {
                EuroLogger.debugLog("reportReceived : Api service could not be found!");
            }

        } else {
            EuroLogger.debugLog("reportReceived : Push Id is invalid!");
        }
    }

    private void reportReceivedRequest(final Retention retention, final int counterId) {
        Call<Void> call1 = retentionApiInterface.report(mUserAgent, retention);
        if(counterId != -1) {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        RetryCounterManager.clearCounter(counterId);
                        Log.i(TAG, "Sending the received report is success");
                    } else {
                        if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                            RetryCounterManager.clearCounter(counterId);
                            Log.e(TAG, "Sending the received report is failed after 3 attempts!!!");
                            call.cancel();
                        } else {
                            RetryCounterManager.increaseCounter(counterId);
                            reportReceivedRequest(retention, counterId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                        RetryCounterManager.clearCounter(counterId);
                        Log.e(TAG, "Sending the received report is failed after 3 attempts!!!");
                        call.cancel();
                    } else {
                        RetryCounterManager.increaseCounter(counterId);
                        reportReceivedRequest(retention, counterId);
                    }
                }
            });
        } else {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Sending the received report is success");
                    } else {
                        Log.e(TAG, "Attempting to send the received report failed!!!");
                        call.cancel();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Attempting to send the received report failed!!!");
                    call.cancel();
                }
            });
        }
    }

    public void sendOpenRequest(Message message) {
        if (Build.VERSION.SDK_INT < Constants.UI_FEATURES_MIN_API) {
            Log.e("Euromessage", "Euromessage SDK requires min API level 21!");
            return;
        }

        if(message == null) {
            Log.e("Sending open report", "Push message is null!");
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    mContext,
                    "e",
                    "Sending open report : " + "Push message is null!",
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            return;
        }

        if (message.getPushId() == null || message.getPushId().isEmpty() || message.getPushId().equals(latestOpenPushId)) {
            Log.e("Euromessage", "Push Id is invalid!");
        } else {
            latestOpenPushId = message.getPushId();
            String emPushSp = message.getEmPushSp();

            if (message.getPushId() != null) {
                EuroLogger.debugLog("Report Read : " + message.getPushId());
                Retention retention = new Retention();

                retention.setKey(subscription.getAppAlias());

                retention.setPushId(message.getPushId());
                retention.setStatus(MessageStatus.Read.toString());
                retention.setToken(SharedPreference.getString(mContext, Constants.TOKEN_KEY));
                retention.setActionBtn(0);
                retention.setDeliver(0);
                retention.setIsMobile(1);
                if (emPushSp != null) {
                    retention.setEmPushSp(emPushSp);
                }

                if (RetentionApiClient.getClient() != null) {
                    retentionApiInterface = RetentionApiClient.getClient().create(EuroApiService.class);
                    reportReadRequest(retention, RetryCounterManager.getCounterId());
                } else {
                    EuroLogger.debugLog("reportRead : Api service could not be found!");
                }
                PayloadUtils.updatePayload(mContext, message.getPushId());
            } else {
                EuroLogger.debugLog("reportRead : Push Id cannot be null!");
            }
        }
    }

    private void reportReadRequest(final Retention retention, final int counterId) {
        Call<Void> call1 = retentionApiInterface.report(mUserAgent, retention);
        if(counterId != -1) {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        RetryCounterManager.clearCounter(counterId);
                        Log.i(TAG, "Sending the read report is success");
                    } else {
                        if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                            RetryCounterManager.clearCounter(counterId);
                            Log.e(TAG, "Sending the read report is failed after 3 attempts!!!");
                            call.cancel();
                        } else {
                            RetryCounterManager.increaseCounter(counterId);
                            reportReadRequest(retention, counterId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                        RetryCounterManager.clearCounter(counterId);
                        Log.e(TAG, "Sending the read report is failed after 3 attempts!!!");
                        call.cancel();
                    } else {
                        RetryCounterManager.increaseCounter(counterId);
                        reportReadRequest(retention, counterId);
                    }
                }
            });
        } else {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Sending the read report is success");
                    } else {
                        Log.e(TAG, "Attempting to send the read report failed!!!");
                        call.cancel();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Attempting to send the read report failed!!!");
                    call.cancel();
                }
            });
        }
    }

    public void subscribe(String token, Context context) {
        this.subscription.setToken(token);

        SharedPreference.saveString(mContext, Constants.TOKEN_KEY, token);

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

        if(this.subscription.getAppAlias() == null || this.subscription.getAppAlias().isEmpty() ||
                this.subscription.getToken() == null || this.subscription.getToken().isEmpty()) {
            Log.e(TAG, "token or appKey cannot be null!");
            return;
        }

        if (this.subscription.isValid(context) && !this.subscription.isEqual(previousSubscription)) {
            previousSubscription = new Subscription();
            previousSubscription.copyFrom(subscription);
            saveSubscription(context);

            try {
                callNetworkSubscription(context);
            } catch (Exception e) {
                StackTraceElement element = new Throwable().getStackTrace()[0];
                LogUtils.formGraylogModel(
                        context,
                        "e",
                        "Sending subscription : " + e.getMessage(),
                        element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                );
                e.printStackTrace();
                callNetworkSubscription(context);
            }

        } else {
            Log.i(TAG, "Not Valid Subs");
        }
    }

    public String getIdentifierForVendor() {
        return subscription.getIdentifierForVendor();
    }

    private void callNetworkSubscription(final Context context) {

        setThreadPolicy();

        if(SubscriptionApiClient.getClient() != null) {
            subscriptionApiInterface = SubscriptionApiClient.getClient().create(EuroApiService.class);
            saveSubscriptionRequest(RetryCounterManager.getCounterId());
        } else {
            EuroLogger.debugLog("saveSubs : Api service could not be found!");
        }
    }

    private void saveSubscriptionRequest(final int counterId) {
        Call<Void> call1 = subscriptionApiInterface.saveSubscription(mUserAgent, subscription);
        if(counterId != -1) {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        RetryCounterManager.clearCounter(counterId);
                        saveSubscriptionNoEmail(mContext);
                        saveSubscriptionDateNoEmail(mContext);
                        Log.i(TAG, "Sending the subscription is success");
                    } else {
                        if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                            RetryCounterManager.clearCounter(counterId);
                            Log.e(TAG, "Sending the subscription is failed after 3 attempts!!!");
                            call.cancel();
                        } else {
                            RetryCounterManager.increaseCounter(counterId);
                            saveSubscriptionRequest(counterId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                        RetryCounterManager.clearCounter(counterId);
                        Log.e(TAG, "Sending the subscription is failed after 3 attempts!!!");
                        call.cancel();
                        t.printStackTrace();
                    } else {
                        RetryCounterManager.increaseCounter(counterId);
                        saveSubscriptionRequest(counterId);
                    }
                }
            });
        } else {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Sending the subscription is success");
                    } else {
                        Log.e(TAG, "An attempt to send the subscription failed!!!");
                        call.cancel();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "An attempt to send the subscription failed!!!");
                    call.cancel();
                }
            });
        }
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

    public void setAppVersion(String appVersion, Context context) {
        this.subscription.setAppVersion(appVersion);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setTwitterId(String twitterId, Context context) {
        setSubscriptionProperty(Constants.EURO_TWITTER_KEY, twitterId, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setEmail(String email, Context context) {

        setSubscriptionProperty(Constants.EURO_EMAIL_KEY, email, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setAnonymous(Boolean permission, Context context) {
        if (permission) {
            setSubscriptionProperty(Constants.EURO_SET_ANONYMOUS_KEY, "true", context);
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
        } else {
            setSubscriptionProperty(Constants.EURO_SET_ANONYMOUS_KEY, "false", context);
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
        }
        sync(context);
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

    /**
     * This method sets an ID to store the last 30-days notifications for a user.
     * To get historical notifications with respect to a specific user
     */
    public void setNotificationLoginID(String notificationLoginId, Context context) {
        SharedPreference.saveString(context, Constants.NOTIFICATION_LOGIN_ID_KEY, notificationLoginId);
    }

    public void setPhoneNumber(String msisdn, Context context) {
        setSubscriptionProperty(Constants.EURO_MSISDN_KEY, msisdn, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void setUserProperty(String key, String value, Context context) {
        setSubscriptionProperty(key, value, context);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void removeUserProperty(Context context, String key) {
        this.subscription.remove(key);
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void removeUserProperties(Context context) {
        this.subscription.removeAll();
        SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
    }

    public void logout(Context context) {
        subscription.setToken(null);
        subscription.setExtra(new HashMap<>());
        SharedPreference.saveString(mContext, Constants.TOKEN_KEY, "");
        saveSubscription(context);
    }

    private void saveSubscription(Context context) {
        try {
            EuroLogger.debugLog(this.subscription.toJson());
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_KEY, this.subscription.toJson());
        } catch (Exception e) {
            Log.e("Exception", "Saving subscription object to shared pref : " + e.getMessage());
            if (BuildConfig.DEBUG) e.printStackTrace();

        }
    }

    private void saveSubscriptionNoEmail(Context context) {
        try {
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_NO_EMAIL_KEY, this.subscription.toJson());
        } catch (Exception e) {
            Log.e("Exception", "Saving subscription object to shared pref : " + e.getMessage());
            if (BuildConfig.DEBUG) e.printStackTrace();

        }
    }

    private void saveSubscriptionWithEmail(Context context, Subscription subscriptionObject) {
        try {
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_WITH_EMAIL_KEY, subscriptionObject.toJson());
        } catch (Exception e) {
            Log.e("Exception", "Saving subscription object to shared pref : " + e.getMessage());
            if (BuildConfig.DEBUG) e.printStackTrace();

        }
    }

    private void saveSubscriptionDateNoEmail(Context context) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String lastSubsDate = dateFormat.format(Calendar.getInstance().getTime());
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_DATE_KEY, lastSubsDate);
        } catch (Exception e) {
            Log.e("Exception", "Saving subscription object to shared pref : " + e.getMessage());
        }
    }

    private void saveSubscriptionDateWithEmail(Context context) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String lastSubsDate = dateFormat.format(Calendar.getInstance().getTime());
            SharedPreference.saveString(context, Constants.EURO_SUBSCRIPTION_DATE_WITH_EMAIL_KEY, lastSubsDate);
        } catch (Exception e) {
            Log.e("Exception", "Saving subscription object to shared pref : " + e.getMessage());
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

    public void setNotificationTransparentSmallIconDarkMode(int transparentSmallIconDarkMode, Context context) {
        if (isResource(context, transparentSmallIconDarkMode)) {
            SharedPreference.saveInt(mContext, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON_DARK_MODE, transparentSmallIconDarkMode);
        } else {
            Log.e("EM : Res Error", transparentSmallIconDarkMode + "");
        }
    }

    public void setNotificationLargeIcon(int largeIcon, Context context) {
        if (isResource(context, largeIcon)) {
            SharedPreference.saveInt(mContext, Constants.NOTIFICATION_LARGE_ICON, largeIcon);
        } else {
            Log.e("EM : Res Error", largeIcon + "");
        }
    }

    public void setNotificationLargeIconDarkMode(int largeIconDarkMode, Context context) {
        if (isResource(context, largeIconDarkMode)) {
            SharedPreference.saveInt(mContext, Constants.NOTIFICATION_LARGE_ICON_DARK_MODE, largeIconDarkMode);
        } else {
            Log.e("EM : Res Error", largeIconDarkMode + "");
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
                Log.e("EM : Res Error", resId + "");

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

    public void setNotificationPriority(RDNotificationPriority priority, Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            String oldChannelId = SharedPreference.getString(context, Constants.NOTIFICATION_CHANNEL_ID_KEY);
            if (!oldChannelId.isEmpty()) {
                notificationManager.deleteNotificationChannel(oldChannelId);
            }
            AppUtils.getNotificationChannelId(context, true);
        }

        String importance = "";
        if(priority == RDNotificationPriority.HIGH) {
            importance = "high";
        } else if(priority == RDNotificationPriority.LOW) {
            importance = "low";
        } else {
            importance = "normal";
        }

        SharedPreference.saveString(context, Constants.NOTIFICATION_PRIORITY_KEY, importance);
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
            registerEmailSubscription.add(Constants.EURO_CONSENT_TIME_KEY, AppUtils.getCurrentTurkeyDateString(mContext));
        } catch (Exception ex) {
            Log.e(TAG, "Cloning subscription object : " + ex.getMessage());

            if(callback != null) {
                callback.fail(ex.getMessage());
            }
            return;
        }

        if(registerEmailSubscription.getAppAlias() == null || registerEmailSubscription.getAppAlias().isEmpty() ||
                registerEmailSubscription.getToken() == null || registerEmailSubscription.getToken().isEmpty()) {
            Log.e(TAG, "token or appKey cannot be null!");
            return;
        }

        if (registerEmailSubscription.isValidWithEmail(context) && !registerEmailSubscription.isEqual(previousRegisterEmailSubscription)) {
            previousRegisterEmailSubscription = new Subscription();
            previousRegisterEmailSubscription.copyFrom(registerEmailSubscription);

            setThreadPolicy();
            if (SubscriptionApiClient.getClient() != null) {
                subscriptionApiInterface = SubscriptionApiClient.getClient().create(EuroApiService.class);
                registerEmailRequest(registerEmailSubscription, RetryCounterManager.getCounterId(), callback);
            } else {
                EuroLogger.debugLog("registerEmail : Api service could not be found!");
            }
        } else {
            Log.i(TAG, "The same email subscription with the previous one!");
        }
    }

    private void registerEmailRequest(final Subscription registerEmailSubscription, final int counterId,
                                      final EuromessageCallback callback) {
        Call<Void> call1 = subscriptionApiInterface.saveSubscription(mUserAgent, registerEmailSubscription);
        if(counterId != -1) {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        RetryCounterManager.clearCounter(counterId);
                        saveSubscriptionWithEmail(mContext, registerEmailSubscription);
                        saveSubscriptionDateWithEmail(mContext);
                        Log.i(TAG, "Register Email Success");
                        if (callback != null) {
                            callback.success();
                        }
                    } else {
                        if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                            RetryCounterManager.clearCounter(counterId);
                            Log.e(TAG, "Registering the email is failed after 3 attempts!!!");
                            call.cancel();
                            if (callback != null) {
                                callback.fail(response.message());
                            }
                        } else {
                            RetryCounterManager.increaseCounter(counterId);
                            registerEmailRequest(registerEmailSubscription, counterId, callback);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (RetryCounterManager.getCounterValue(counterId) >= 3) {
                        RetryCounterManager.clearCounter(counterId);
                        Log.e(TAG, "Registering the email is failed after 3 attempts!!!");
                        call.cancel();
                        t.printStackTrace();
                        if (callback != null) {
                            callback.fail(t.getMessage());
                        }
                    } else {
                        RetryCounterManager.increaseCounter(counterId);
                        registerEmailRequest(registerEmailSubscription, counterId, callback);
                    }
                }
            });
        } else {
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Register Email Success");
                        if (callback != null) {
                            callback.success();
                        }
                    } else {
                        Log.e(TAG, "An attempt to register the email failed!!!");
                        call.cancel();
                        if (callback != null) {
                            callback.fail(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "An attempt to register the email failed!!!");
                    call.cancel();
                    if (callback != null) {
                        callback.fail(t.getMessage());
                    }
                }
            });
        }
    }

    public static boolean deleteAllPushNotifications(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
            return true;
        }
        return false;
    }

    public static boolean deletePushNotificationWithId(Context context, Integer notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && notificationId != null) {
            notificationManager.cancel(notificationId);
            return true;
        }
        return false;
    }

    public static boolean readPushMessages(@Nullable String pushId) {
        if (mContext == null) {
            Log.e(LOG_TAG, "Context cannot be null!");
            return false;
        }

        String jsonString = SharedPreference.getString(mContext, Constants.PAYLOAD_SP_KEY);
        if (jsonString == null || jsonString.isEmpty()) {
            Log.e(LOG_TAG, "Payload string is null or empty!");
            return false;
        }

        try {
            JSONObject payloadData = new JSONObject(jsonString);
            JSONArray payloadsArray = payloadData.optJSONArray(Constants.PAYLOAD_SP_ARRAY_KEY);

            if (payloadsArray == null || payloadsArray.length() == 0) {
                Log.e(LOG_TAG, "Payload array is null or empty!");
                return false;
            }

            boolean isUpdated = false;
            boolean shouldUpdateAll = (pushId == null || pushId.isEmpty() || pushId.trim().isEmpty());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            if (!shouldUpdateAll) {

                for (int i = 0; i < payloadsArray.length(); i++) {
                    JSONObject payloadObject = payloadsArray.getJSONObject(i);
                    String existingPushId = payloadObject.optString("pushId", "");

                    if (existingPushId.equals(pushId)) {
                        payloadObject.put("status", "O");
                        payloadObject.put("openDate", currentDate);
                        isUpdated = true;
                        break;
                    }
                }

                if (!isUpdated) {

                    shouldUpdateAll = true;
                }
            }

            if (shouldUpdateAll) {
                for (int i = 0; i < payloadsArray.length(); i++) {
                    JSONObject payloadObject = payloadsArray.getJSONObject(i);
                    payloadObject.put("status", "O");
                    payloadObject.put("openDate", currentDate);
                }
                isUpdated = true;
            }

            if (isUpdated) {
                payloadData.put(Constants.PAYLOAD_SP_ARRAY_KEY, payloadsArray);
                SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_KEY, payloadData.toString());

                return true;
            }

            Log.e(LOG_TAG, "No updates were made to the push messages.");
            return false;
        } catch (JSONException e) {
            if (e.getStackTrace().length > 0) {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage() + " e.getStackTrace().length > 0");
            } else {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage());
            }
            Log.e(LOG_TAG, "Could not update the push messages!");
            Log.e(LOG_TAG, e.getMessage());
            return false;
        } catch (Exception e) {
            if (e.getStackTrace().length > 0) {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage() + " e.getStackTrace().length > 0");
            } else {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage());
            }
            Log.e(LOG_TAG, "Could not update the push messages!");
            Log.e(LOG_TAG, e.getMessage());
            return false;
        }
    }

    public static boolean readPushMessagesWithId(@Nullable String pushId) {
        if (mContext == null) {
            Log.e(LOG_TAG, "Context cannot be null!");
            return false;
        }

        String jsonString = SharedPreference.getString(mContext, Constants.PAYLOAD_SP_ID_KEY);
        if (jsonString == null || jsonString.isEmpty()) {
            Log.e(LOG_TAG, "Payload string is null or empty!");
            return false;
        }

        try {
            JSONObject payloadData = new JSONObject(jsonString);
            JSONArray payloadsArray = payloadData.optJSONArray(Constants.PAYLOAD_SP_ARRAY_ID_KEY);

            if (payloadsArray == null || payloadsArray.length() == 0) {
                Log.e(LOG_TAG, "Payload array is null or empty!");
                return false;
            }

            boolean isUpdated = false;
            boolean shouldUpdateAll = (pushId == null || pushId.trim().isEmpty());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            if (!shouldUpdateAll) {

                for (int i = 0; i < payloadsArray.length(); i++) {
                    JSONObject payloadObject = payloadsArray.getJSONObject(i);
                    String existingPushId = payloadObject.optString("pushId", "");

                    if (existingPushId.equals(pushId)) {
                        payloadObject.put("status", "O");
                        payloadObject.put("openDate", currentDate);
                        isUpdated = true;
                        break;
                    }
                }

                if (!isUpdated) {
                    shouldUpdateAll = true;
                }
            }

            if (shouldUpdateAll) {
                for (int i = 0; i < payloadsArray.length(); i++) {
                    JSONObject payloadObject = payloadsArray.getJSONObject(i);
                    payloadObject.put("status", "O");
                    payloadObject.put("openDate", currentDate);
                }
                isUpdated = true;
            }

            if (isUpdated) {
                payloadData.put(Constants.PAYLOAD_SP_ARRAY_ID_KEY, payloadsArray);
                SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_ID_KEY, payloadData.toString());

                return true;
            }

            Log.e(LOG_TAG, "No updates were made to the push messages.");
            return false;
        } catch (JSONException e) {
            if (e.getStackTrace().length > 0) {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage() + " e.getStackTrace().length > 0");
            } else {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage());
            }
            Log.e(LOG_TAG, "Could not update the push messages!");
            Log.e(LOG_TAG, e.getMessage());
            return false;
        } catch (Exception e) {
            if (e.getStackTrace().length > 0) {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage() + " e.getStackTrace().length > 0");
            } else {
                Log.e(LOG_TAG, "JSONException: " + e.getMessage());
            }
            Log.e(LOG_TAG, "Could not update the push messages!");
            Log.e(LOG_TAG, e.getMessage());
            return false;
        }
    }

    /**
     * This method returns the list of push messages sent in the last 30 days.
     * The messages are ordered in terms of their timestamps e.g. most recent one is at index 0.
     * activity : Activity
     * callback : PushMessageInterface
     */
    public void getPushMessages(final Activity activity, final PushMessageInterface callback) {
        if(callback == null || activity == null) {
            Log.w("EM-getPushMessages() : ", "callback or activity cannot be null!");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String payloads = SharedPreference.getString(mContext, Constants.PAYLOAD_SP_KEY);
                if(!payloads.isEmpty()) {
                    try {
                        final List<Message> pushMessages = new ArrayList<Message>();
                        JSONObject jsonObject = new JSONObject(payloads);
                        JSONArray jsonArray = jsonObject.getJSONArray(Constants.PAYLOAD_SP_ARRAY_KEY);
                        for(int i = 0 ; i < jsonArray.length() ; i++) {
                            JSONObject currentObject = jsonArray.getJSONObject(i);
                            Message currentMessage = new Gson().fromJson(currentObject.toString(), Message.class);
                            pushMessages.add(currentMessage);
                        }
                        final List<Message> orderedPushMessages = PayloadUtils.orderPushMessages(mContext, pushMessages);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.success(orderedPushMessages);
                            }
                        });
                    } catch (final Exception e) {
                        SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_KEY, "");
                        Log.e(LOG_TAG, "De-serializing JSON string of push message : " + e.getMessage());
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.fail(e.getMessage());
                            }
                        });
                    }
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.fail("There is not any push notification sent " +
                                    "(or saved) in the last 30 days");
                        }
                    });
                }
            }
        }) {
        }.start();
    }

    /**
     * This method returns the list of push messages sent to the user logged-in in the last 30 days.
     * The messages are ordered in terms of their timestamps e.g. most recent one is at index 0.
     * activity : Activity
     * callback : PushMessageInterface
     */
    public void getPushMessagesWithID(final Activity activity, final PushMessageInterface callback) {
        if(callback == null || activity == null) {
            Log.e("EM-getPushMessages() : ", "callback or activity cannot be null!");
            return;
        }
        String loginID = SharedPreference.getString(mContext, Constants.NOTIFICATION_LOGIN_ID_KEY);
        if(loginID.isEmpty()) {
            Log.e("EM-getPushMessages() : ", "login ID is empty!");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String payloads = SharedPreference.getString(mContext, Constants.PAYLOAD_SP_ID_KEY);
                if(!payloads.isEmpty()) {
                    try {
                        final List<Message> pushMessages = new ArrayList<Message>();
                        JSONObject jsonObject = new JSONObject(payloads);
                        JSONArray jsonArray = jsonObject.getJSONArray(Constants.PAYLOAD_SP_ARRAY_ID_KEY);
                        for(int i = 0 ; i < jsonArray.length() ; i++) {
                            JSONObject currentObject = jsonArray.getJSONObject(i);
                            Message currentMessage = new Gson().fromJson(currentObject.toString(), Message.class);
                            if(currentMessage.getLoginID() != null && !currentMessage.getLoginID().isEmpty()) {
                                if(loginID.equals(currentMessage.getLoginID())) {
                                    pushMessages.add(currentMessage);
                                }
                            }
                        }
                        final List<Message> orderedPushMessages = PayloadUtils.orderPushMessages(mContext, pushMessages);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.success(orderedPushMessages);
                            }
                        });
                    } catch (final Exception e) {
                        SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_ID_KEY, "");
                        Log.e(LOG_TAG, "De-serializing JSON string of push message : " + e.getMessage());
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.fail(e.getMessage());
                            }
                        });
                    }
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.fail("There is not any push notification sent " +
                                    "(or saved) in the last 30 days");
                        }
                    });
                }
            }
        }) {
        }.start();
    }

    public boolean deletePushMessagesWithId(@Nullable String messageId) {
        String payloads = SharedPreference.getString(mContext, Constants.PAYLOAD_SP_ID_KEY);
        if (!payloads.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payloads);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.PAYLOAD_SP_ARRAY_ID_KEY);
                JSONArray newJsonArray = new JSONArray();

                boolean messageFound = false;

                if (messageId != null && !messageId.isEmpty()) {
                    // messageId sağlanmışsa, sadece ilgili mesajı silmeye çalış
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject currentObject = jsonArray.getJSONObject(i);
                        Message currentMessage = new Gson().fromJson(currentObject.toString(), Message.class);
                        if (!currentMessage.getPushId().equals(messageId)) {
                            newJsonArray.put(currentObject);
                        } else {
                            messageFound = true;
                        }
                    }

                    if (messageFound) {
                        // Sadece ilgili mesaj silindi, güncellenmiş array'i kaydet
                        jsonObject.put(Constants.PAYLOAD_SP_ARRAY_ID_KEY, newJsonArray);
                        SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_ID_KEY, jsonObject.toString());
                        return true;
                    } else {
                        // messageId bulunamadı, tüm mesajları sil
                        jsonObject.put(Constants.PAYLOAD_SP_ARRAY_ID_KEY, newJsonArray);
                        SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_ID_KEY, jsonObject.toString());
                        return true;
                    }
                } else {
                    // messageId sağlanmamışsa, tüm mesajları sil
                    jsonObject.put(Constants.PAYLOAD_SP_ARRAY_ID_KEY, newJsonArray);
                    SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_ID_KEY, jsonObject.toString());
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean deletePushMessages(@Nullable String messageId) {
        String payloads = SharedPreference.getString(mContext, Constants.PAYLOAD_SP_KEY);
        if (!payloads.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payloads);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.PAYLOAD_SP_ARRAY_KEY);
                JSONArray newJsonArray = new JSONArray();

                boolean messageFound = false;

                if (messageId != null && !messageId.isEmpty()) {
                    // messageId sağlanmışsa, sadece ilgili mesajı silmeye çalış
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject currentObject = jsonArray.getJSONObject(i);
                        Message currentMessage = new Gson().fromJson(currentObject.toString(), Message.class);
                        if (!currentMessage.getPushId().equals(messageId)) {
                            newJsonArray.put(currentObject);
                        } else {
                            messageFound = true;
                        }
                    }

                    if (messageFound) {
                        // Sadece ilgili mesaj silindi, güncellenmiş array'i kaydet
                        jsonObject.put(Constants.PAYLOAD_SP_ARRAY_KEY, newJsonArray);
                        SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_KEY, jsonObject.toString());
                        return true;
                    } else {
                        // messageId bulunamadı, tüm mesajları sil
                        jsonObject.put(Constants.PAYLOAD_SP_ARRAY_KEY, newJsonArray);
                        SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_KEY, jsonObject.toString());
                        return true;
                    }
                } else {
                    // messageId sağlanmamışsa, tüm mesajları sil
                    jsonObject.put(Constants.PAYLOAD_SP_ARRAY_KEY, newJsonArray);
                    SharedPreference.saveString(mContext, Constants.PAYLOAD_SP_KEY, jsonObject.toString());
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public void requestNotificationPermission(Context context, PushNotificationPermissionInterface callback) {
        if (Build.VERSION.SDK_INT >= 33) {
            NotificationPermissionActivity.callback = granted -> {
                setPushPermit(granted ? PushPermit.ACTIVE : PushPermit.PASSIVE, context);
                sync(context);
                callback.success(granted);
            };
            Intent intent = new Intent(context, NotificationPermissionActivity.class);
            context.startActivity(intent);
        }
    }

    public void requestNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            NotificationPermissionActivity.callback = granted -> {
                setPushPermit(granted ? PushPermit.ACTIVE : PushPermit.PASSIVE, context);
                sync(context);
            };
            Intent intent = new Intent(context, NotificationPermissionActivity.class);
            context.startActivity(intent);
        }
    }


    private void fillGraylogModel() {
        graylogModel = new GraylogModel();
        if (checkPlayService(mContext)) {
            graylogModel.setGoogleAppAlias(subscription.getAppAlias());
        } else {
            graylogModel.setHuaweiAppAlias(subscription.getAppAlias());
        }
        graylogModel.setToken(subscription.getToken());
        graylogModel.setAppVersion(subscription.getAppVersion());
        graylogModel.setSdkVersion(subscription.getSdkVersion());
        graylogModel.setOsType(subscription.getOs());
        graylogModel.setOsVersion(subscription.getOsVersion());
        graylogModel.setDeviceName(subscription.getDeviceName());
        graylogModel.setUserAgent(mUserAgent);
        graylogModel.setIdentifierForVendor(subscription.getIdentifierForVendor());
        graylogModel.setExtra(subscription.getExtra());
    }

    public void sendLogToGraylog(String logLevel, String logMessage, String logPlace) {
        GraylogModel graylogModel = this.graylogModel;
        graylogModel.setLogLevel(logLevel);
        graylogModel.setLogMessage(logMessage);
        graylogModel.setLogPlace(logPlace);
        LogUtils.sendGraylogMessage(graylogModel);
    }
}