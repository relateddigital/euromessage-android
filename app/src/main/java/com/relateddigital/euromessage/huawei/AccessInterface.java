package com.relateddigital.euromessage.huawei;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AccessInterface {
    @FormUrlEncoded
    @POST("v2/token")
    Call<AccessToken> GetAccessToken(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret);
}