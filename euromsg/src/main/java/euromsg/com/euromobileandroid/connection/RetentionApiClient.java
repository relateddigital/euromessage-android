package euromsg.com.euromobileandroid.connection;

import android.os.Build;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.connection.interceptor.RawResponseInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetentionApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (Build.VERSION.SDK_INT < Constants.UI_FEATURES_MIN_API) {
            Log.e("Euromessage", "Euromessage SDK requires min API level 21!");
            return null;
        }

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(new RawResponseInterceptor())
                        .addInterceptor(interceptor).connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(45, TimeUnit.SECONDS);

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://pushr.euromsg.com/")
                    .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build())
                    .build();
        }

        return retrofit;
    }
}