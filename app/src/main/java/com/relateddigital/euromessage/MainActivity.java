package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;


public class MainActivity extends AppCompatActivity {

    private static EuroMobileManager euroMobileManager;

    public static String APP_ALIAS = Constants.APP_ALIAS;
    
    String token;

    AutoCompleteTextView autotext;
    Button btnSync,btnText,btnImage, btnCarousel;
    TextView tvTokenMessage, tvToken, tvRelease;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);

        autotext = findViewById(R.id.autotext);
        btnSync = findViewById(R.id.btn_sync);
        btnCarousel = findViewById(R.id.btn_carousel);
        btnText  = findViewById(R.id.btn_text);
        btnImage = findViewById(R.id.btn_image);
        tvRelease = findViewById(R.id.tvRelease);
        tvToken = findViewById(R.id.tv_token);
        tvTokenMessage = findViewById(R.id.tv_token_message);

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

        euroMobileManager.registerToFCM(getBaseContext());
    }

    private void setUI() {

        sendATemplatePush();

        setReleaseName();

        checkTokenStatus();

        sync();
    }

    private void sync() {

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (autotext.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Email", Toast.LENGTH_LONG).show();

                } else {
                    euroMobileManager.setEmail(autotext.getText().toString().trim(), getApplicationContext());
                    euroMobileManager.setEuroUserId("12345", getApplicationContext());
                    euroMobileManager.sync(getApplicationContext());
                    autotext.setText("");
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
                            tvTokenMessage.setText(getResources().getString(R.string.fail_token));
                            tvTokenMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            return;
                        }

                        token = task.getResult().getToken();
                        tvTokenMessage.setText(getResources().getString(R.string.success_token));
                        tvTokenMessage.setTextColor(getResources().getColor(R.color.colorButton));
                        tvToken.setText(token);
                    }
                });
    }

    public void setReleaseName() {

        String libVersionName = euromsg.com.euromobileandroid.BuildConfig.VERSION_NAME;

        tvRelease.setText("Appv : " + BuildConfig.VERSION_NAME + " " + " EM SDKv: " + libVersionName);
    }

    public void sendATemplatePush() {

        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testText, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, null);
            }
        });


        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testImage, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, AppUtils.getBitMapFromUri((message.getMediaUrl())));
            }
        });

        btnCarousel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testCarousel, Message.class);
                pushNotificationManager.generateCarouselNotification(getApplicationContext(), message);

            }
        });
    }
}