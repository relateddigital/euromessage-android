package com.relateddigital.euromessage;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import euromsg.com.euromobileandroid.EuroMobileManager;

public class SecondActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_def);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras() != null) {
            EuroMobileManager.getInstance().reportRead(intent.getExtras());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getExtras() != null && EuroMobileManager.getInstance().getNotification(getIntent()) != null) {
            EuroMobileManager.getInstance().reportRead(getIntent().getExtras());
        }
    }

}
