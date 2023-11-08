package com.relateddigital.euromessage;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.enums.RDNotificationPriority;

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
        euroMobileManager.setNotificationTransparentSmallIconDarkMode(R.drawable.delete_icon_dark_mode, getApplicationContext());
        euroMobileManager.useNotificationLargeIcon(true);
        euroMobileManager.setNotificationLargeIcon(R.drawable.euromessage, getApplicationContext());
        euroMobileManager.setNotificationLargeIconDarkMode(R.drawable.related_digital_dark_mode, getApplicationContext());
        euroMobileManager.setNotificationColor("#d1dbbd");
        euroMobileManager.setChannelName("Demo", getApplicationContext());
        euroMobileManager.setPushIntent("com.relateddigital.euromessage.MainActivity", getApplicationContext());
        euroMobileManager.setNotificationPriority(RDNotificationPriority.NORMAL, getApplicationContext());



            setExistingFirebaseTokenToEuroMessage();



    }

    private void setExistingFirebaseTokenToEuroMessage() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("Firebase Token : ", "Getting the token failed!!!");
                        return;
                    }
                    String token = task.getResult();
                    euroMobileManager.subscribe(token, getApplicationContext());

                    SP.saveString(getApplicationContext(), "FirebaseToken", token);
                });
    }


}
