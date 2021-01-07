package com.relateddigital.euromessage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.visilabs.Visilabs;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.enums.EmailPermit;
import euromsg.com.euromobileandroid.enums.GsmPermit;
import euromsg.com.euromobileandroid.model.EuromessageCallback;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;


public class MainActivity extends AppCompatActivity {

    private static final int FIRST_ITEM_CAROUSEL = 0;

    AutoCompleteTextView autotext, registeremailAutotext;
    Button btnSync, btnText, btnImage, btnCarousel, btnRegisteremail;
    TextView tvRelease, tvLastPushTime;
    EditText etFirebaseToken, etHuaweiToken;
    Spinner registeremailCommercialSpinner, registeremailPermitSpinner;
    TableLayout tlPushParams;

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
        etFirebaseToken = findViewById(R.id.et_token);
        etHuaweiToken = findViewById(R.id.et_huawei_token);

        registeremailAutotext = findViewById(R.id.registeremail_autotext);
        btnRegisteremail = findViewById(R.id.btn_registeremail);
        registeremailCommercialSpinner = findViewById(R.id.registeremailcommercial_spinner);
        registeremailPermitSpinner = findViewById(R.id.registeremailpermit_spinner);
        tlPushParams = findViewById(R.id.tl_push_params);
        tvLastPushTime = findViewById(R.id.tv_last_push_time);

        createSpinners();
        visilabsAdvertisement();
        setUI();
        setPushParamsUI();
    }

    private void createSpinners() {
        String[] registeremailCommercialSpinnerItems = new String[]{ Constants.EURO_RECIPIENT_TYPE_BIREYSEL, Constants.EURO_RECIPIENT_TYPE_TACIR };
        ArrayAdapter aa1 = new ArrayAdapter(this,android.R.layout.simple_spinner_item, registeremailCommercialSpinnerItems);
        aa1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        registeremailCommercialSpinner.setAdapter(aa1);

        String[] registeremailPermitSpinnerItems = new String[]{ Constants.EMAIL_PERMIT_ACTIVE, Constants.EMAIL_PERMIT_PASSIVE };
        ArrayAdapter aa2 = new ArrayAdapter(this,android.R.layout.simple_spinner_item, registeremailPermitSpinnerItems);
        aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        registeremailPermitSpinner.setAdapter(aa2);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            Message message = (Message) intent.getExtras().getSerializable("message");
            handlePush(message, intent);
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String lastPushTime = dateFormat. format(Calendar.getInstance().getTime());
        SP.saveString(getApplicationContext(), Constants.LAST_PUSH_TIME, lastPushTime);
        SP.saveString(getApplicationContext(), Constants.LAST_PUSH_PARAMS, new GsonBuilder().create().toJson(message.getParams()));
        for (Map.Entry<String, String> entry : message.getParams().entrySet()) {
            Log.d( "Push Params", "Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
        EuroMobileManager.getInstance().reportRead(intent.getExtras());
        notificationUrl(intent);
    }

    private void setPushParamsUI() {
        int rowCount = tlPushParams.getChildCount();
        if (rowCount > 2) {
            tlPushParams.removeViews(2, rowCount - 2);
        }
        tvLastPushTime.setText(SP.getString(getApplicationContext(), Constants.LAST_PUSH_TIME));
        String lastPushParamsString = SP.getString(getApplicationContext(), Constants.LAST_PUSH_PARAMS);
        Gson gson = new Gson();
        Type paramsType = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> params = gson.fromJson(lastPushParamsString, paramsType);
        int index = 0;
        for(Map.Entry<String, String> param : params.entrySet()){
            setTableRowUI(index, param);
            index++;
        }
    }

    private void setTableRowUI(int index, Map.Entry<String, String> param) {
        int backgroundColor =  (index %2 ==0) ? R.color.colorLight : R.color.colorBackground;
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

        tlPushParams.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }


    private void setUI() {
        sendATemplatePush();
        etHuaweiToken.setText(SP.getString(getApplicationContext(), "HuaweiToken"));
        etFirebaseToken.setText(SP.getString(getApplicationContext(), "FirebaseToken"));
        tvRelease.setText("Appv : " + BuildConfig.VERSION_NAME + " " + " EM SDKv: " + BuildConfig.VERSION_NAME);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync();
            }
        });
        btnRegisteremail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerEmail();
            }
        });
    }

    private void registerEmail() {
        if (registeremailAutotext.getText().toString().equals("")) {
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
                    if(errorMessage != null) {
                        message = message + errorMessage;
                    }
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            };

            boolean isCommercial = false;
            String isCommercialText = String.valueOf(registeremailCommercialSpinner.getSelectedItem());
            if(isCommercialText.equals(Constants.EURO_RECIPIENT_TYPE_TACIR)) {
                isCommercial = true;
            }

            EmailPermit emailPermit = EmailPermit.ACTIVE;
            String isEmailPermitActiveText = String.valueOf(registeremailPermitSpinner.getSelectedItem());
            if(isEmailPermitActiveText.equals(Constants.EMAIL_PERMIT_PASSIVE)) {
                emailPermit = EmailPermit.PASSIVE;
            }

            EuroMobileManager.getInstance().registerEmail(registeremailAutotext.getText().toString().trim(), emailPermit, isCommercial, getApplicationContext(), callback);
            Toast.makeText(getApplicationContext(), "Check RMC", Toast.LENGTH_LONG).show();
        }
    }

    private void sync() {

        if (autotext.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter Email", Toast.LENGTH_LONG).show();

        } else {
            EuroMobileManager.getInstance().setGsmPermit(GsmPermit.ACTIVE, getApplicationContext());
            EuroMobileManager.getInstance().setEmail(autotext.getText().toString().trim(), getApplicationContext());
            EuroMobileManager.getInstance().setEuroUserId("12345", getApplicationContext());
            EuroMobileManager.getInstance().sync(getApplicationContext());
            Toast.makeText(getApplicationContext(), "Check RMC", Toast.LENGTH_LONG).show();
        }
    }

    public void sendATemplatePush() {

        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int notificationId = new Random().nextInt();
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testText, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, null, notificationId);
            }
        });


        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int notificationId = new Random().nextInt();
                PushNotificationManager pushNotificationManager = new PushNotificationManager();
                Message message = new Gson().fromJson(TestPush.testImage, Message.class);
                pushNotificationManager.generateNotification(getApplicationContext(), message, AppUtils.getBitmap(message.getMediaUrl()),notificationId);
            }
        });

        btnCarousel.setOnClickListener(new View.OnClickListener() {
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

        Visilabs.CreateAPI(Constants.ORGANIZATION_ID, Constants.SITE_ID, "http://lgr.visilabs.net",
                Constants.DATASOURCE, "http://rt.visilabs.net", "Android", getApplicationContext(), "http://s.visilabs.net/json", "http://s.visilabs.net/actjson", 30000, "http://s.visilabs.net/geojson", true);

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
}