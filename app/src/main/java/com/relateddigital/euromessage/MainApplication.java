package com.relateddigital.euromessage;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import euromsg.com.euromobileandroid.EuroMobileManager;

public class MainApplication extends Application {

    public static String APP_ALIAS = Constants.APP_ALIAS;

    EuroMobileManager euroMobileManager;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeEuroMessage();
    }

    public void initializeEuroMessage() {

        euroMobileManager = EuroMobileManager.init("euromessage-android" , "euromsg-huawei", getApplicationContext());
        euroMobileManager.registerToFCM(getBaseContext());

        //optional
        euroMobileManager.setNotificationTransparentSmallIcon(android.R.drawable.star_off, getApplicationContext());
        euroMobileManager.setNotificationColor("#d1dbbd");
        euroMobileManager.setChannelName("Demo", getApplicationContext());

        setExistingFirebaseTokenToEuroMessage();

        setHuaweiTokenToEuromessage();
    }

    private void setExistingFirebaseTokenToEuroMessage() {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (!task.isSuccessful()) {
                            return;
                        }

                        String token = task.getResult().getToken();
                        euroMobileManager.subscribe(token, getApplicationContext());

                        SP.saveString(getApplicationContext(),"FirebaseToken", token);
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

                        SP.saveString(getApplicationContext(),"HuaweiToken", token);

                        Log.i("Huawei Token",  "" + token);

                    } catch (ApiException e) {
                        Log.e("Huawei Token", "get token failed, " + e);
                    }
                }
            }.start();
    }
}
