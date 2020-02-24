package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.carousalnotification.CarousalEventReceiver;
import euromsg.com.euromobileandroid.model.Message;


public class MainActivity extends AppCompatActivity {

    private static EuroMobileManager euroMobileManager;

    public static String ENTEGRASYON_ID = TestConstant.ENTEGRASYON_ID;

    TextView tvToken;
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeEuroMessage();

        setRelease();

        tvToken = findViewById(R.id.tv_token);
        etEmail = findViewById(R.id.et_email);

        Button button = findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncExample();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras() != null) {
            euroMobileManager.reportRead(new Message(intent.getExtras()));
        }
    }

    public void initializeEuroMessage() {

        euroMobileManager = EuroMobileManager.sharedManager(ENTEGRASYON_ID, getApplicationContext());

        euroMobileManager.registerToFCM(getBaseContext());

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            tvToken.setText("Token Alınamadı");
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.e("token", token);

                        tvToken.setText("Token Alındı : " + token);
                    }
                });

    }

    public void syncExample() {

        if (!etEmail.getText().toString().equals("")) {
            euroMobileManager.setEmail(etEmail.getText().toString().trim(), getApplicationContext());
            euroMobileManager.sync(getApplicationContext());
            Toast.makeText(getApplicationContext(), "Sync", Toast.LENGTH_LONG).show();
        }
    }


    public void setRelease () {

        TextView tvAppRelease = findViewById(R.id.tvAppRelease);
        TextView tvSDKRelease = findViewById(R.id.tvSDKRelease);

        String libVersionName = euromsg.com.euromobileandroid.BuildConfig.VERSION_NAME;

        tvAppRelease.setText("App Version : " + BuildConfig.VERSION_NAME );
        tvSDKRelease.setText(" EuroMessage SDK Version: " + libVersionName);
    }
}