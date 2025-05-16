package euromsg.com.euromobileandroid.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
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

public class EuroFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        String googleAppAlias;
        String huaweiAppAlias;
        try {
            EuroLogger.debugLog("On new token : " + token);
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if(appInfo!=null){
                Bundle bundle = appInfo.metaData;
                if(bundle!=null){
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

            EuroLogger.debugLog(e.toString());
            googleAppAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);
            huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);
            EuroMobileManager.init(googleAppAlias, huaweiAppAlias, this).subscribe(token, this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> remoteMessageData = remoteMessage.getData();
        if(remoteMessageData.isEmpty()) {
            Log.e("FMessagingService", "Push message is empty!");
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    this,
                    "e",
                    "FMessagingService : " + "Push message is empty!",
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            return;
        }
        Message pushMessage = new Message(this, remoteMessageData);

        if(pushMessage.getEmPushSp() == null) {
            Log.i("Push Notification", "The push notification was not coming from Related Digital! Ignoring..");
            return;
        }

        EuroLogger.debugLog("EM FirebasePayload : " + new Gson().toJson(pushMessage));

        PushNotificationManager pushNotificationManager = new PushNotificationManager();

        EuroLogger.debugLog("Message received : " + pushMessage.getMessage());

        String appAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);
        String huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);

        EuroMobileManager euroMobileManager = EuroMobileManager.init(appAlias, huaweiAppAlias, this);

        if(pushMessage.getSilent() != null && pushMessage.getSilent().equalsIgnoreCase("true")) {
            Log.i("EuroFirabase", "Silent Push");
            euroMobileManager.reportReceived(pushMessage.getPushId(),
                    pushMessage.getEmPushSp(), true);
        } else {

            if (pushMessage.getPushType() != null && pushMessage.getPushId() != null) {

                int notificationId = new Random().nextInt(Integer.MAX_VALUE);
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

                if(notificationLoginId.isEmpty()) {
                    PayloadUtils.addPushMessage(this, pushMessage);
                } else {
                    PayloadUtils.addPushMessageWithId(this, pushMessage, notificationLoginId);
                }
                PayloadUtils.updatePayloadWithId(this,pushMessage.getPushId(),notificationId);

            } else {
                EuroLogger.debugLog("remoteMessageData transfrom problem");
            }
        }
    }
}