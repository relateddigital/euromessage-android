package euromsg.com.euromobileandroid.service;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> remoteMessageData = remoteMessage.getData();
        Message pushMessage = new Message(this, remoteMessageData);
        EuroLogger.debugLog("EM FirebasePayload : " + new Gson().toJson(pushMessage));
        Intent intent = AppUtils.getLaunchIntent(getApplicationContext(), pushMessage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}