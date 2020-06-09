package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.visilabs.Visilabs;

import java.util.HashMap;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.enums.GsmPermit;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;


public class MainActivity extends AppCompatActivity {

    private static final int FIRST_ITEM_CAROUSEL = 0;

    private static EuroMobileManager euroMobileManager;

    public static String APP_ALIAS = Constants.APP_ALIAS;

    String token;

    AutoCompleteTextView autotext;
    Button btnSync, btnText, btnImage, btnCarousel, btnInapp;
    TextView tvTokenMessage, tvToken, tvRelease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autotext = findViewById(R.id.autotext);
        btnSync = findViewById(R.id.btn_sync);
        btnCarousel = findViewById(R.id.btn_carousel);
        btnText = findViewById(R.id.btn_text);
        btnImage = findViewById(R.id.btn_image);
        tvRelease = findViewById(R.id.tvRelease);
        tvToken = findViewById(R.id.tv_token);
        tvTokenMessage = findViewById(R.id.tv_token_message);

        initializeEuroMessage();

        visilabsAdvertisement();

        setUI();

        getHuaweiToken();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras() != null) {
            euroMobileManager.reportRead(intent.getExtras());
            notificationUrl(intent);
        }
    }

    private void notificationUrl(Intent intent) {

        if (euroMobileManager.getNotification(intent) != null) {

            Log.d("Euromessage",euroMobileManager.getNotification(intent).getUrl());
        }

        if (euroMobileManager.getCarousels(intent) != null) {

            Log.d("Euromessage Carousel", euroMobileManager.getCarousels(intent).get(FIRST_ITEM_CAROUSEL).getUrl());
        }

        euroMobileManager.removeIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getExtras() != null && euroMobileManager.getNotification(getIntent()) != null) {
            euroMobileManager.reportRead(getIntent().getExtras());
            notificationUrl(getIntent());
        }
    }

    public void initializeEuroMessage() {

        euroMobileManager = EuroMobileManager.init(APP_ALIAS, this);
        euroMobileManager.registerToFCM(getBaseContext());

        euroMobileManager.setNotificationTransparentSmallIcon(android.R.drawable.star_off, getApplicationContext());
        euroMobileManager.setNotificationColor("#d1dbbd");

       // euroMobileManager.removeNotificationColor();
      //  euroMobileManager.removeNotificationTransparentSmallIcon();
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
                    euroMobileManager.setGsmPermit(GsmPermit.ACTIVE, getApplicationContext());
                    euroMobileManager.setEmail(autotext.getText().toString().trim(), getApplicationContext());
                    euroMobileManager.setEuroUserId("12345", getApplicationContext());
                    euroMobileManager.sync(getApplicationContext());
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
                            EuroMobileManager.getInstance().subscribe(token, getApplicationContext());
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

    private void visilabsAdvertisement() {

        final String exVisitorId = "testUser@test.com";

        Visilabs.CreateAPI(Constants.ORGANIZATION_ID, Constants.SITE_ID, "http://lgr.visilabs.net",
                Constants.DATASOURCE, "http://rt.visilabs.net", "Android", getApplicationContext(),  "http://s.visilabs.net/json", "http://s.visilabs.net/actjson", 30000, "http://s.visilabs.net/geojson", true);

        Button btnInnApp = findViewById(R.id.btn_in_app);
        btnInnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("OM.exVisitorID", exVisitorId);
                Visilabs.CallAPI().customEvent("android-visilab", parameters, MainActivity.this);
            }
        });

        Button btnInfo = findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.visilabs.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }


    private void getHuaweiToken() {

        Button huaweiBtn = findViewById(R.id.btn_huawei);

        huaweiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            // read from agconnect-services.json
                            String appId = AGConnectServicesConfig.fromContext(MainActivity.this).getString("client/app_id");
                            String token = HmsInstanceId.getInstance(MainActivity.this).getToken(appId, "HCM");
                            Log.i("Huawei Token", "get token:" + token);
                            if(!TextUtils.isEmpty(token)) {
                                sendRegTokenToServer(token);
                            }

                        } catch (ApiException e) {
                            Log.e("Huawei Token", "get token failed, " + e);
                        }
                    }
                }.start();
            }
        });
    }

    private void sendRegTokenToServer(String token) {
        Log.i("TAG", "sending token to server. token:" + token);
    }
}