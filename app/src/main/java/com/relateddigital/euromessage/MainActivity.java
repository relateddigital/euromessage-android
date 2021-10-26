package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setUI();
    }

    private void setUI() {
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

        binding.date.setText(SP.getString(getApplicationContext(), Constants.LAST_PUSH_TIME));
        String lastPushParamsString = SP.getString(getApplicationContext(), Constants.LAST_PUSH_PARAMS);
        if(lastPushParamsString.isEmpty() || lastPushParamsString.equals("{}")) {
            binding.payload.setText("empty payload!!");
        } else {
            Gson gson = new Gson();
            Type paramsType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> params = gson.fromJson(lastPushParamsString, paramsType);
            if (params == null) {
                binding.payload.setText("empty payload!!");
            } else {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> param : params.entrySet()) {
                    sb.append(param.getKey()).append(" : ").append(param.getValue()).append("\n\n");
                }
                binding.payload.setText(sb.toString());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Message message = (Message) intent.getExtras().getSerializable("message");
                handlePush(message);
            }
        }
    }

    private void handlePush(Message message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String lastPushTime = dateFormat.format(Calendar.getInstance().getTime());
        SP.saveString(getApplicationContext(), Constants.LAST_PUSH_TIME, lastPushTime);
        SP.saveString(getApplicationContext(), Constants.LAST_PUSH_PARAMS, new GsonBuilder().create().toJson(message.getParams()));
    }

    private void getFirabaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            binding.etToken.setText("an error occurred when getting token!!");
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