package com.relateddigital.euromessage.huawei;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotiInterface {
    @Headers("Content-Type:application/json")
    @POST("appid/messages:send")//Appid sizin bilginiz olmalı.
    Call<PushResult> sendNotification(
            @Header("Authorization") String authorization,
// Bearer $accessToken
            @Body NotificationMessage notificationMessage);
}