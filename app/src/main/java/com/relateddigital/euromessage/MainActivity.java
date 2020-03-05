package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.relateddigital.euromessage.databinding.ActivityMainBinding;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.connection.ConnectionManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;


public class MainActivity extends AppCompatActivity {

    private static EuroMobileManager euroMobileManager;

    public static String APP_ALIAS = Constants.APP_ALIAS;

    ActivityMainBinding mainBinding;

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initializeEuroMessage();

        setUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras() != null) {
            euroMobileManager.reportRead(new Message(intent.getExtras()));
        }
    }

    public void initializeEuroMessage() {

        euroMobileManager = EuroMobileManager.createInstance(APP_ALIAS, this);

        euroMobileManager.registerToFCM(getBaseContext());

        euroMobileManager.setEmail("test@euromsg.com", this);
        euroMobileManager.setEuroUserId("12345", this);
        euroMobileManager.sync(this);
    }

    private void setUI() {
        checkTokenStatus();

        sendATemplatePush();

        setReleaseName();
    }

    private void checkTokenStatus() {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (!task.isSuccessful()) {
                            mainBinding.tvTokenMessage.setText(getResources().getString(R.string.fail_token));
                            return;
                        }

                        token = task.getResult().getToken();
                        mainBinding.tvTokenMessage.setText(getResources().getString(R.string.success_token));

                        mainBinding.tvToken.setText(token);
                    }
                });
    }

    public void setReleaseName() {

        String libVersionName = euromsg.com.euromobileandroid.BuildConfig.VERSION_NAME;

        mainBinding.tvAppRelease.setText("App Version : " + BuildConfig.VERSION_NAME);
        mainBinding.tvSDKRelease.setText(" EuroMessage SDK Version: " + libVersionName);
    }

    public void sendATemplatePush() {

        mainBinding.btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testText, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, null);
            }
        });


        mainBinding.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testImage, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, ConnectionManager.getInstance().getBitMapFromUri((message.getMediaUrl())));
            }
        });

        mainBinding.btnCarousel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testCarousel, Message.class);
                pushNotificationManager.generateCarouselNotification(getApplicationContext(), message);

            }
        });
    }
}