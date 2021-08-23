package euromsg.com.euromobileandroid.service;

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


import java.util.Map;
import java.util.Random;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;

import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.EuroLogger;
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

            PayloadUtils.addPushMessage(this, pushMessage);
        } else {
            EuroLogger.debugLog("remoteMessageData transfrom problem");
        }
    }
}