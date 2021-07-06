package euromsg.com.euromobileandroid.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;


import java.util.Map;
import java.util.Random;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;

import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class EuroFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        try {
            EuroLogger.debugLog("On new token : " + token);
            String gooleAppAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);
            String huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);
            EuroMobileManager.init(gooleAppAlias, huaweiAppAlias, this).subscribe(token, this);

        } catch (Exception e) {
            EuroLogger.debugLog(e.toString());
            EuroLogger.debugLog("Failed to complete token refresh");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> remoteMessageData = remoteMessage.getData();
        Message pushMessage = new Message(remoteMessageData);

        if(pushMessage.getEmPushSp() == null) {
            Log.i("Push Notification", "The push notification was not coming from Related Digital! Ignoring..");
            return;
        }

        EuroLogger.debugLog("EM FirebasePayload : " + new Gson().toJson(pushMessage));

        PushNotificationManager pushNotificationManager = new PushNotificationManager();

        EuroLogger.debugLog("Message received : " + pushMessage.getMessage());

        if (pushMessage.getPushType() != null && pushMessage.getPushId() != null) {


            int notificationId = new Random().nextInt();

            switch (pushMessage.getPushType()) {

                case Image:

                    if (pushMessage.getElements() != null) {
                        pushNotificationManager.generateCarouselNotification(this, pushMessage, notificationId);
                    } else {
                        pushNotificationManager.generateNotification(this, pushMessage, AppUtils.getBitMapFromUri(pushMessage.getMediaUrl()),notificationId);
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
            String appAlias = SharedPreference.getString(this, Constants.GOOGLE_APP_ALIAS);
            String huaweiAppAlias = SharedPreference.getString(this, Constants.HUAWEI_APP_ALIAS);

            EuroMobileManager.init(appAlias, huaweiAppAlias, this).reportReceived(pushMessage.getPushId(),
                    pushMessage.getEmPushSp());
        } else {
            EuroLogger.debugLog("remoteMessageData transfrom problem");
        }
    }
}