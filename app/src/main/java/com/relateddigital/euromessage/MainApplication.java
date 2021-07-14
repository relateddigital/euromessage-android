package com.relateddigital.euromessage;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import euromsg.com.euromobileandroid.EuroMobileManager;

public class MainApplication extends Application {

    EuroMobileManager euroMobileManager;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeEuroMessage();
    }

    public void initializeEuroMessage() {

        euroMobileManager = EuroMobileManager.init(Constants.GOOGLE_APP_ALIAS, Constants.HUAWEI_APP_ALIAS, getApplicationContext());
        euroMobileManager.registerToFCM(getBaseContext());

        //optional
        euroMobileManager.setNotificationTransparentSmallIcon(android.R.drawable.star_off, getApplicationContext());
        euroMobileManager.useNotificationLargeIcon(true);
        euroMobileManager.setNotificationLargeIcon(R.drawable.related_digital, getApplicationContext());
        euroMobileManager.setNotificationColor("#d1dbbd");
        euroMobileManager.setChannelName("RelatedDigitalDemo", getApplicationContext());
        euroMobileManager.setPushIntent("com.relateddigital.euromessage.MainActivity", getApplicationContext());

        setExistingFirebaseTokenToEuroMessage();

        if (!EuroMobileManager.checkPlayService(getApplicationContext())) {
            setHuaweiTokenToEuromessage();
        }
    }

    private void setExistingFirebaseTokenToEuroMessage() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult();
                        euroMobileManager.subscribe(token, getApplicationContext());

                        SP.saveString(getApplicationContext(), "FirebaseToken", token);
                    }
                });
    }

    private void setHuaweiTokenToEuromessage() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(getApplicationContext()).getString("client/app_id");
                    final String token = HmsInstanceId.getInstance(getApplicationContext()).getToken(appId, "HCM");

                    euroMobileManager.subscribe(token, getApplicationContext());

                    SP.saveString(getApplicationContext(), "HuaweiToken", token);

                    Log.i("Huawei Token", "" + token);

                } catch (ApiException e) {
                    Log.e("Huawei Token", "get token failed, " + e);
                }
            }
        }.start();
    }
}
