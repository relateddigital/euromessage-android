package euromsg.com.euromobileandroid.service;








import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

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

public class EuroMsgFCMHelper {

    public static void onMessageReceived(Context context, RemoteMessage remoteMessage) {

        Map<String, String> remoteMessageData = remoteMessage.getData();
        if(remoteMessageData.isEmpty()) {
            Log.e("FMessagingService", "Push message is empty!");
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "FMessagingService : " + "Push message is empty!",
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            return;
        }
        Message pushMessage = new Message(context, remoteMessageData);

        if(pushMessage.getEmPushSp() == null) {
            Log.i("Push Notification", "The push notification was not coming from Related Digital! Ignoring..");
            return;
        }

        EuroLogger.debugLog("EM FirebasePayload : " + new Gson().toJson(pushMessage));

        PushNotificationManager pushNotificationManager = new PushNotificationManager();

        EuroLogger.debugLog("Message received : " + pushMessage.getMessage());

        String appAlias = SharedPreference.getString(context, Constants.GOOGLE_APP_ALIAS);
        String huaweiAppAlias = SharedPreference.getString(context, Constants.HUAWEI_APP_ALIAS);

        EuroMobileManager euroMobileManager = EuroMobileManager.init(appAlias, huaweiAppAlias, context);

        if(pushMessage.getSilent() != null && pushMessage.getSilent().equalsIgnoreCase("true")) {
            Log.i("EuroFirabase", "Silent Push");
            euroMobileManager.reportReceived(pushMessage.getPushId(),
                    pushMessage.getEmPushSp(), true);
        } else {

            if (pushMessage.getPushType() != null && pushMessage.getPushId() != null) {

                int notificationId = new Random().nextInt(Integer.MAX_VALUE);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
                    String channelName = SharedPreference.getString(context, Constants.NOTIFICATION_CHANNEL_NAME_KEY);
                    String channelDescription = SharedPreference.getString(context, Constants.NOTIFICATION_CHANNEL_DESCRIPTION_KEY);
                    String channelSound = SharedPreference.getString(context, Constants.NOTIFICATION_CHANNEL_SOUND_KEY);

                    if (!channelName.equals(PushNotificationManager.getChannelName(context)) ||
                            !channelDescription.equals(PushNotificationManager.getChannelDescription(context)) ||
                            !channelSound.equals(pushMessage.getSound())) {
                        String oldChannelId = SharedPreference.getString(context, Constants.NOTIFICATION_CHANNEL_ID_KEY);
                        if (!oldChannelId.isEmpty()) {
                            notificationManager.deleteNotificationChannel(oldChannelId);
                        }
                        AppUtils.getNotificationChannelId(context, true);
                    } else {
                        AppUtils.getNotificationChannelId(context, false);
                    }
                    SharedPreference.saveString(context, Constants.NOTIFICATION_CHANNEL_NAME_KEY, PushNotificationManager.getChannelName(context));
                    SharedPreference.saveString(context, Constants.NOTIFICATION_CHANNEL_DESCRIPTION_KEY, PushNotificationManager.getChannelDescription(context));
                    SharedPreference.saveString(context, Constants.NOTIFICATION_CHANNEL_SOUND_KEY, pushMessage.getSound());
                }

                switch (pushMessage.getPushType()) {

                    case Image:

                        if (pushMessage.getElements() != null) {
                            pushNotificationManager.generateCarouselNotification(context, pushMessage, notificationId);
                        } else {
                            pushNotificationManager.generateNotification(context, pushMessage, AppUtils.getBitMapFromUri(context, pushMessage.getMediaUrl()), notificationId);
                        }

                        break;

                    case Text:
                        pushNotificationManager.generateNotification(context, pushMessage, null, notificationId);

                        break;

                    case Video:
                        break;

                    default:
                        pushNotificationManager.generateNotification(context, pushMessage, null, notificationId);
                        break;
                }

                if (pushMessage.getDeliver() != null &&
                        pushMessage.getDeliver().toLowerCase(Locale.ROOT).equals("true")) {
                    euroMobileManager.reportReceived(pushMessage.getPushId(),
                            pushMessage.getEmPushSp(), false);
                }

                String notificationLoginId = SharedPreference.getString(context, Constants.NOTIFICATION_LOGIN_ID_KEY);

                if(notificationLoginId.isEmpty()) {
                    PayloadUtils.addPushMessage(context, pushMessage);
                } else {
                    PayloadUtils.addPushMessageWithId(context, pushMessage, notificationLoginId);
                }
                PayloadUtils.updatePayloadWithId(context,pushMessage.getPushId(),notificationId);

            } else {
                EuroLogger.debugLog("remoteMessageData transfrom problem");
            }
        }
    }


    public static void onNewToken(Context context,@NonNull String token) {
        String googleAppAlias;
        String huaweiAppAlias;
        try {
            EuroLogger.debugLog("On new token : " + token);
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if(appInfo!=null){
                Bundle bundle = appInfo.metaData;
                if(bundle!=null){
                    googleAppAlias = bundle.getString("GoogleAppAlias", "");
                    huaweiAppAlias = bundle.getString("HuaweiAppAlias", "");
                } else {
                    googleAppAlias = SharedPreference.getString(context, Constants.GOOGLE_APP_ALIAS);
                    huaweiAppAlias = SharedPreference.getString(context, Constants.HUAWEI_APP_ALIAS);
                }
            } else {
                googleAppAlias = SharedPreference.getString(context, Constants.GOOGLE_APP_ALIAS);
                huaweiAppAlias = SharedPreference.getString(context, Constants.HUAWEI_APP_ALIAS);
            }
            EuroMobileManager.init(googleAppAlias, huaweiAppAlias, context).subscribe(token, context);

        } catch (Exception e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Reading app alias from manifest file : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            EuroLogger.debugLog(e.toString());
            googleAppAlias = SharedPreference.getString(context, Constants.GOOGLE_APP_ALIAS);
            huaweiAppAlias = SharedPreference.getString(context, Constants.HUAWEI_APP_ALIAS);
            EuroMobileManager.init(googleAppAlias, huaweiAppAlias, context).subscribe(token, context);
        }
    }
}
