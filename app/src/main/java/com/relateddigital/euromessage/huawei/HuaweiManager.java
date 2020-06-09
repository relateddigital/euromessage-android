package com.relateddigital.euromessage.huawei;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.huawei.hms.api.Api;
import com.relateddigital.euromessage.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HuaweiManager {

    private static final String ACCESS_TOKEN = "https://login.cloud.huawei.com/oauth2/";//(Base URL)
    private static final String NOTI_URL = "https://push-api.cloud.huawei.com/v1/";

    public void getAccessToken(final String token, final Context context){ // Access Token alacağınız fonksiyon
        AccessInterface apiInterface = ApiClient.getClient(ACCESS_TOKEN).create(AccessInterface.class);
        Call<AccessToken> call = apiInterface.GetAccessToken("client_credentials","378063343241397376","client_secret"); // Bu bilgileri sizin doldurmanız gerekecek.
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) { // Bu kısımda Bearer koymamızın //nedeni Push Api'nin Bearer+accesstoken şeklinde bir yapı istemesi
                String  accesstoken = null;
                if (response.body() != null) {
                    accesstoken = "Bearer "+response.body().getAccessToken();
                }
                sendNotification(accesstoken, token, context);
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void sendNotification(String accesstoken, String token, Context context){
        NotiInterface notiInterface = ApiClient.getClient(NOTI_URL).create(NotiInterface.class);
        Intent intent = new Intent(context, MainActivity.class);
        NotificationMessage notificationMessage = (new NotificationMessage.Builder("Title","Başarılı",token,intent.toString())).build();
        Call<PushResult> callNoti = notiInterface.sendNotification(accesstoken,notificationMessage);

        callNoti.enqueue(new Callback<PushResult>() {
            @Override
            public void onResponse(@NonNull Call<PushResult> call, @NonNull Response<PushResult> response) {
                if (response.body() != null) {
                    Log.i("callnoti", response.body().getMsg());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PushResult> call, @NonNull Throwable t) {
                Log.i("callnoti",t.toString());
            }
        });
    }
}
