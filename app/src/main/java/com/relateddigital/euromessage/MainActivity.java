package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.relateddigital.euromessage.databinding.ActivityMainBinding;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;


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
            euroMobileManager.reportRead(intent.getExtras());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void initializeEuroMessage() {

        euroMobileManager = EuroMobileManager.init(APP_ALIAS, this);

        euroMobileManager.registerToFCM(this);
    }

    private void setUI() {

        sendATemplatePush();

        setReleaseName();

        checkTokenStatus();

        sync();
    }

    private void sync() {

        mainBinding.btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mainBinding.autotext.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Email", Toast.LENGTH_LONG).show();

                } else {
                    euroMobileManager.setEmail(mainBinding.autotext.getText().toString().trim(), getApplicationContext());
                    euroMobileManager.setEuroUserId("12345", getApplicationContext());
                    euroMobileManager.sync(getApplicationContext());
                    mainBinding.autotext.setText("");
                    Toast.makeText(getApplicationContext(), "Check RMC", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void checkTokenStatus() {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (!task.isSuccessful()) {
                            mainBinding.tvTokenMessage.setText(getResources().getString(R.string.fail_token));
                            mainBinding.tvTokenMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            return;
                        }

                        token = task.getResult().getToken();
                        mainBinding.tvTokenMessage.setText(getResources().getString(R.string.success_token));
                        mainBinding.tvTokenMessage.setTextColor(getResources().getColor(R.color.colorButton));
                        mainBinding.tvToken.setText(token);
                    }
                });
    }

    public void setReleaseName() {

        String libVersionName = euromsg.com.euromobileandroid.BuildConfig.VERSION_NAME;

        mainBinding.tvRelease.setText("Appv : " + BuildConfig.VERSION_NAME + " " + " EM SDKv: " + libVersionName);
    }

    public void sendATemplatePush() {

        mainBinding.btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testText, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, null, null);
            }
        });


        mainBinding.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testImage, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, AppUtils.getBitMapFromUri((message.getMediaUrl())), null);
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