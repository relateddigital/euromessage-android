package euromsg.com.euromobileandroid.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.LogUtils;
import euromsg.com.euromobileandroid.utils.PayloadUtils;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class EuroHuaweiMessagingService extends HmsMessageService {

    private static final String TAG = "EuroMessage Huawei";

    @Override
    public void onNewToken(String token) {
        String googleAppAlias;
        String huaweiAppAlias;
        if (!checkPlayService()) {
            if (!TextUtils.isEmpty(token)) {
                try {
                    EuroLogger.debugLog("On new token : " + token);
                    ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                    if (appInfo != null) {
                        Bundle bundle = appInfo.metaData;
                        if (bundle != null) {
                            googleAppAlias = bundle.getString("GoogleAppAlias", "");
                            huaweiAppAlias = bundle.getString("HuaweiAppAlias", "");
                        } else {
                            googleAppAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);
                            huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);
                        }
                    } else {
                        googleAppAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);
                        huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);
                    }
                    EuroMobileManager.init(googleAppAlias, huaweiAppAlias, this).subscribe(token, this);

                } catch (Exception e) {
                    StackTraceElement element = new Throwable().getStackTrace()[0];
                    LogUtils.formGraylogModel(
                            this,
                            "e",
                            "Reading app alias from manifest file : " + e.getMessage(),
                            element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                    );
                    EuroLogger.debugLog(e.toString());
                    googleAppAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);
                    huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);
                    EuroMobileManager.init(googleAppAlias, huaweiAppAlias, this).subscribe(token, this);
                }
                Log.i(TAG, "Huawei Token refresh token:" + token);
            }
        } else {
            Log.i(TAG, "HMS is not enable");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (!checkPlayService()) {
            Log.i(TAG, "onMessageReceived is called");
            if (remoteMessage == null) {
                Log.e(TAG, "Received message entity is null!");
                return;
            }

            Log.i("Euromessage HPayload", remoteMessage.getData());

            Map<String, String> remoteMessageData = remoteMessage.getDataOfMap();
            if (remoteMessageData.isEmpty()) {
                Log.e("HMessagingService", "Push message is empty!");
                StackTraceElement element = new Throwable().getStackTrace()[0];
                LogUtils.formGraylogModel(
                        this,
                        "e",
                        "HMessagingService : " + "Push message is empty!",
                        element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                );
                return;
            }
            Message pushMessage = new Message(this, remoteMessageData);

            if (pushMessage.getEmPushSp() == null) {
                Log.i("Push Notification", "The push notification was not coming from Related Digital! Ignoring..");
                return;
            }

            PushNotificationManager pushNotificationManager = new PushNotificationManager();

            EuroLogger.debugLog("Message received : " + pushMessage.getMessage());

            String huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);
            String googleAppAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);

            EuroMobileManager euroMobileManager = EuroMobileManager.init(googleAppAlias, huaweiAppAlias, this);

            if (pushMessage.getSilent() != null && pushMessage.getSilent().equals("true")) {
                Log.i("EuroHuawei", "Silent Push");
                euroMobileManager.reportReceived(pushMessage.getPushId(),
                        pushMessage.getEmPushSp(), true);
            } else {

                if (pushMessage.getPushType() != null && pushMessage.getPushId() != null) {

                    int notificationId = new Random().nextInt();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
                        String channelName = SharedPreference.getString(this, Constants.NOTIFICATION_CHANNEL_NAME_KEY);
                        String channelDescription = SharedPreference.getString(this, Constants.NOTIFICATION_CHANNEL_DESCRIPTION_KEY);
                        String channelSound = SharedPreference.getString(this, Constants.NOTIFICATION_CHANNEL_SOUND_KEY);

                        if (!channelName.equals(PushNotificationManager.getChannelName(this)) ||
                                !channelDescription.equals(PushNotificationManager.getChannelDescription(this)) ||
                                !channelSound.equals(pushMessage.getSound())) {
                            String oldChannelId = SharedPreference.getString(this, Constants.NOTIFICATION_CHANNEL_ID_KEY);
                            if (!oldChannelId.isEmpty()) {
                                notificationManager.deleteNotificationChannel(oldChannelId);
                            }
                            AppUtils.getNotificationChannelId(this, true);
                        } else {
                            AppUtils.getNotificationChannelId(this, false);
                        }
                        SharedPreference.saveString(this, Constants.NOTIFICATION_CHANNEL_NAME_KEY, PushNotificationManager.getChannelName(this));
                        SharedPreference.saveString(this, Constants.NOTIFICATION_CHANNEL_DESCRIPTION_KEY, PushNotificationManager.getChannelDescription(this));
                        SharedPreference.saveString(this, Constants.NOTIFICATION_CHANNEL_SOUND_KEY, pushMessage.getSound());
                    }

                    switch (pushMessage.getPushType()) {

                        case Image:

                            if (pushMessage.getElements() != null) {
                                pushNotificationManager.generateCarouselNotification(this, pushMessage, notificationId);
                            } else {
                                pushNotificationManager.generateNotification(this, pushMessage, AppUtils.getBitMapFromUri(this, pushMessage.getMediaUrl()), notificationId);
                            }

                            break;

                        case Text:
                            pushNotificationManager.generateNotification(this, pushMessage, null, notificationId);

                            break;

                        case Video:
                            break;

                        default:
                            pushNotificationManager.generateNotification(this, pushMessage, null, notificationId);
                            break;
                    }

                    if (pushMessage.getDeliver() != null &&
                            pushMessage.getDeliver().toLowerCase(Locale.ROOT).equals("true")) {
                        euroMobileManager.reportReceived(pushMessage.getPushId(),
                                pushMessage.getEmPushSp(), false);
                    }

                    String notificationLoginId = SharedPreference.getString(this, Constants.NOTIFICATION_LOGIN_ID_KEY);

                    if (notificationLoginId.isEmpty()) {
                        PayloadUtils.addPushMessage(this, pushMessage);
                    } else {
                        PayloadUtils.addPushMessageWithId(this, pushMessage, notificationLoginId);
                    }
                } else {
                    EuroLogger.debugLog("remoteMessageData transform problem");
                }
            }
        } else {
            Log.i(TAG, "Google Services are enable");
        }
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);

        Log.e(TAG, "Token is not generated" + e.toString());
    }


    private boolean checkPlayService() {
        boolean result = true;

        int isGoogleEnabled = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

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

                Log.e(TAG, "Google Service is enable");

                break;
        }

        return result;
    }
}