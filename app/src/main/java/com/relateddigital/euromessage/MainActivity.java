package com.relateddigital.euromessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;


public class MainActivity extends AppCompatActivity {

    private static EuroMobileManager euroMobileManager;

    public static String ENTEGRASYON_ID = "euromessage-android"; //Push uygulamları sekmesinde uygulamanızın entegrasyon id'si

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeEuroMessage();

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

    public void initializeEuroMessage(){

        euroMobileManager = EuroMobileManager.sharedManager(ENTEGRASYON_ID);

        euroMobileManager.registerToFCM(getBaseContext());
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("token", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.d("token", token);
                    }
                });

    }

    public void syncExample() {


        euroMobileManager.setEmail("test@testew.com", getApplicationContext());
        euroMobileManager.sync(getApplicationContext());
    }
}
