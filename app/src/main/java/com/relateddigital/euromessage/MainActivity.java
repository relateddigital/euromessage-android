package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.relateddigital.euromessage.databinding.ActivityMainBinding;
import com.visilabs.Visilabs;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.callback.PushMessageInterface;
import euromsg.com.euromobileandroid.enums.EmailPermit;
import euromsg.com.euromobileandroid.enums.GsmPermit;
import euromsg.com.euromobileandroid.model.CarouselItem;
import euromsg.com.euromobileandroid.model.EuromessageCallback;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;


public class MainActivity extends AppCompatActivity {

    private static final int FIRST_ITEM_CAROUSEL = 0;
    private ActivityMainBinding binding;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        activity = this;
        createSpinners();
        visilabsAdvertisement();
        setUI();
        setPushParamsUI();
    }

    private void createSpinners() {
        String[] registeremailCommercialSpinnerItems = new String[]{Constants.EURO_RECIPIENT_TYPE_BIREYSEL, Constants.EURO_RECIPIENT_TYPE_TACIR};
        ArrayAdapter aa1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, registeremailCommercialSpinnerItems);
        aa1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.registeremailcommercialSpinner.setAdapter(aa1);

        String[] registeremailPermitSpinnerItems = new String[]{Constants.EMAIL_PERMIT_ACTIVE, Constants.EMAIL_PERMIT_PASSIVE};
        ArrayAdapter aa2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, registeremailPermitSpinnerItems);
        aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.registeremailpermitSpinner.setAdapter(aa2);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Message message = (Message) intent.getExtras().getSerializable("message");
                if(message != null) {
                    handlePush(message, intent);
                } else {
                    // Carousel push notification : an item was clicked
                    String itemClickedUrl = bundle.getString("CarouselItemClickedUrl");
                    if(itemClickedUrl != null && !itemClickedUrl.equals("")) {
                        try {
                            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemClickedUrl));
                            viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(viewIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        setPushParamsUI();
    }

    private void notificationUrl(Intent intent) {

        if (EuroMobileManager.getInstance().getNotification(intent) != null) {
            Log.d("Euromessage", EuroMobileManager.getInstance().getNotification(intent).getUrl());
        }

        if (EuroMobileManager.getInstance().getCarousels(intent) != null) {
            Log.d("Euromessage Carousel", EuroMobileManager.getInstance().getCarousels(intent).get(FIRST_ITEM_CAROUSEL).getUrl());
        }

        EuroMobileManager.getInstance().removeIntentExtra(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getExtras() != null && EuroMobileManager.getInstance().getNotification(getIntent()) != null) {
            Message message = (Message) getIntent().getExtras().getSerializable("message");
            handlePush(message, getIntent());
        }
        setPushParamsUI();
    }

    private void handlePush(Message message, Intent intent) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String lastPushTime = dateFormat.format(Calendar.getInstance().getTime());
        SP.saveString(getApplicationContext(), Constants.LAST_PUSH_TIME, lastPushTime);
        SP.saveString(getApplicationContext(), Constants.LAST_PUSH_PARAMS, new GsonBuilder().create().toJson(message.getParams()));
        if (message.getParams() != null) {
            for (Map.Entry<String, String> entry : message.getParams().entrySet()) {
                Log.d("Push Params", "Key : " + entry.getKey() + " Value : " + entry.getValue());
            }
        }
        notificationUrl(intent);
    }

    private void setPushParamsUI() {
        int rowCount = binding.tlPushParams.getChildCount();
        if (rowCount > 2) {
            binding.tlPushParams.removeViews(2, rowCount - 2);
        }
        binding.tvLastPushTime.setText(SP.getString(getApplicationContext(), Constants.LAST_PUSH_TIME));
        String lastPushParamsString = SP.getString(getApplicationContext(), Constants.LAST_PUSH_PARAMS);
        Gson gson = new Gson();
        Type paramsType = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> params = gson.fromJson(lastPushParamsString, paramsType);
        if (params == null)
            return;
        int index = 0;
        for (Map.Entry<String, String> param : params.entrySet()) {
            setTableRowUI(index, param);
            index++;
        }
    }

    private void setTableRowUI(int index, Map.Entry<String, String> param) {
        int backgroundColor = (index % 2 == 0) ? R.color.colorLight : R.color.colorBackground;
        TableRow tr = new TableRow(this);
        String key = param.getKey().length() > 30 ? param.getKey().substring(0, 30) : param.getKey();
        String value = param.getValue().length() > 30 ? param.getValue().substring(0, 30) : param.getValue();


        final TextView tvKey = new TextView(this);
        tvKey.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tvKey.setGravity(Gravity.CENTER);
        tvKey.setTextSize(16);
        tvKey.setPadding(8, 8, 8, 8);
        tvKey.setBackgroundColor(getResources().getColor(backgroundColor));
        tvKey.setTextColor(Color.BLACK);
        tvKey.setText(key);

        final TextView tvValue = new TextView(this);
        tvValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tvValue.setGravity(Gravity.CENTER);
        tvValue.setTextSize(16);
        tvValue.setPadding(8, 8, 8, 8);
        tvValue.setBackgroundColor(getResources().getColor(backgroundColor));
        tvValue.setTextColor(Color.BLACK);
        tvValue.setText(value);

        tr.addView(tvKey);
        tr.addView(tvValue);

        binding.tlPushParams.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }


    private void setUI() {
        sendATemplatePush();
        String huaweiToken = SP.getString(getApplicationContext(), "HuaweiToken");
        String firabaseToken = SP.getString(getApplicationContext(), "FirebaseToken");

        if (EuroMobileManager.checkPlayService(getApplicationContext())) {
            if (firabaseToken.equals("")) {
                getFirabaseToken();
            } else {
                binding.etToken.setText(firabaseToken);
            }
        } else {
            if (huaweiToken.equals("")) {
                getHuaweiToken();
            } else {
                binding.etHuaweiToken.setText(huaweiToken);
            }
        }

        binding.tvRelease.setText("Appv : " + com.relateddigital.euromessage.BuildConfig.VERSION_NAME + " " + " EM SDKv: " + euromsg.com.euromobileandroid.BuildConfig.VERSION_NAME);

        binding.btnPayload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushMessageInterface pushMessageInterface = new PushMessageInterface() {
                    @Override
                    public void success(List<Message> pushMessages) {
                        Toast.makeText(getApplicationContext(), "Payloads are gotten successfully!", Toast.LENGTH_SHORT).show();
                        // Make your implementation by using pushMessages here:
                    }

                    @Override
                    public void fail(String errorMessage) {
                        Toast.makeText(getApplicationContext(), "Trying to get the payloads failed!", Toast.LENGTH_SHORT).show();
                        // Something went wrong. You may consider warning the user:
                    }
                };
                EuroMobileManager.getInstance().getPushMessages(activity, pushMessageInterface);
            }
        });
        binding.btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync();
            }
        });
        binding.btnRegisteremail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerEmail();
            }
        });
    }

    private void registerEmail() {
        if (binding.registeremailAutotext.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter Email", Toast.LENGTH_LONG).show();

        } else {
            EuromessageCallback callback = new EuromessageCallback() {
                @Override
                public void success() {
                    Toast.makeText(getApplicationContext(), "REGISTER EMAIL SUCCESS", Toast.LENGTH_LONG).show();
                }

                @Override
                public void fail(String errorMessage) {
                    String message = "REGISTER EMAIL ERROR";
                    if (errorMessage != null) {
                        message = message + errorMessage;
                    }
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            };

            boolean isCommercial = false;
            String isCommercialText = String.valueOf(binding.registeremailcommercialSpinner.getSelectedItem());
            if (isCommercialText.equals(Constants.EURO_RECIPIENT_TYPE_TACIR)) {
                isCommercial = true;
            }

            EmailPermit emailPermit = EmailPermit.ACTIVE;
            String isEmailPermitActiveText = String.valueOf(binding.registeremailpermitSpinner.getSelectedItem());
            if (isEmailPermitActiveText.equals(Constants.EMAIL_PERMIT_PASSIVE)) {
                emailPermit = EmailPermit.PASSIVE;
            }

            EuroMobileManager.getInstance().registerEmail(binding.registeremailAutotext.getText().toString().trim(), emailPermit, isCommercial, getApplicationContext(), callback);
            Toast.makeText(getApplicationContext(), "Check RMC", Toast.LENGTH_LONG).show();
        }
    }

    private void sync() {

        if (binding.autotext.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter Email", Toast.LENGTH_LONG).show();

        } else {
            EuroMobileManager.getInstance().setGsmPermit(GsmPermit.ACTIVE, getApplicationContext());
            EuroMobileManager.getInstance().setEmail(binding.autotext.getText().toString().trim(), getApplicationContext());
            EuroMobileManager.getInstance().setEuroUserId("12345", getApplicationContext());
            EuroMobileManager.getInstance().sync(getApplicationContext());
            Toast.makeText(getApplicationContext(), "Check RMC", Toast.LENGTH_LONG).show();
        }
    }

    public void sendATemplatePush() {

        binding.btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int notificationId = new Random().nextInt();
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testText, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, null, notificationId);
            }
        });


        binding.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int notificationId = new Random().nextInt();
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testImage, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, AppUtils.getBitMapFromUri(getApplicationContext(), message.getMediaUrl()), notificationId);
            }
        });

        binding.btnCarousel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int notificationId = new Random().nextInt();
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testCarousel, Message.class);
                pushNotificationManager.generateCarouselNotification(getApplicationContext(), message, notificationId);

            }
        });
    }

    private void visilabsAdvertisement() {

        final String exVisitorId = "testUser@test.com";

        Visilabs.CreateAPI(Constants.ORGANIZATION_ID, Constants.SITE_ID, "https://lgr.visilabs.net",
                Constants.DATASOURCE, "https://rt.visilabs.net", "Android", getApplicationContext(), "http://s.visilabs.net/json", "http://s.visilabs.net/actjson", 30000, "http://s.visilabs.net/geojson", true);

        binding.btnInApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("OM.exVisitorID", exVisitorId);
                Visilabs.CallAPI().customEvent("android-visilab", parameters, MainActivity.this);
                Toast.makeText(getApplicationContext(), "Check RMC", Toast.LENGTH_LONG).show();
            }
        });

        binding.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.visilabs.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    private void getFirabaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        binding.etToken.setText(task.getResult());
                    }
                });
    }

    private void getHuaweiToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(getApplicationContext()).getString("client/app_id");
                    final String token = HmsInstanceId.getInstance(getApplicationContext()).getToken(appId, "HCM");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.etHuaweiToken.setText(token);
                        }
                    });

                } catch (ApiException e) {
                    Log.e("Huawei Token", "get token failed, " + e);
                }
            }
        }.start();
    }
}