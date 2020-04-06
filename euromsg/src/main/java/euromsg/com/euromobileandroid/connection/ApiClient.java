package euromsg.com.euromobileandroid.connection;

import euromsg.com.euromobileandroid.connection.interceptor.APIKeyInterceptor;
import euromsg.com.euromobileandroid.connection.interceptor.RawResponseInterceptor;
import euromsg.com.euromobileandroid.enums.BaseUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(BaseUrl baseUrl) {

        OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(new RawResponseInterceptor())
                        .addInterceptor(new APIKeyInterceptor());

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl.toString())
                    .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build())
                    .build();
        }

        return retrofit;
    }
}