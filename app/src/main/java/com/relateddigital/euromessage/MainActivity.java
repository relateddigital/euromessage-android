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
import com.relateddigital.euromessage.databinding.ActivityMainBinding;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;


public class MainActivity extends AppCompatActivity {

    private static EuroMobileManager euroMobileManager;

    public static String ENTEGRASYON_ID = TestConstant.ENTEGRASYON_ID;

    ActivityMainBinding mainBinding;

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initializeEuroMessage();

        setReleaseName();

        mainBinding.btnSync.setOnClickListener(new View.OnClickListener() {
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

        checkTokenStatus();
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

    public void syncExample() {

        if (!mainBinding.etEmail.getText().toString().equals("")) {
            euroMobileManager.setEmail(mainBinding.etEmail.getText().toString().trim(), getApplicationContext());
            euroMobileManager.sync(getApplicationContext());

            Toast.makeText(getApplicationContext(), "Sync", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "E-Mail Adresi Giriniz", Toast.LENGTH_LONG).show();
        }
    }


    public void setReleaseName() {

        String libVersionName = euromsg.com.euromobileandroid.BuildConfig.VERSION_NAME;

        mainBinding.tvAppRelease.setText("App Version : " + BuildConfig.VERSION_NAME );
        mainBinding.tvSDKRelease.setText(" EuroMessage SDK Version: " + libVersionName);
    }
}