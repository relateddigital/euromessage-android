package euromsg.com.euromobileandroid.connection;

import android.os.Build;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import euromsg.com.euromobileandroid.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SubscriptionApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (Build.VERSION.SDK_INT < Constants.UI_FEATURES_MIN_API) {
            Log.e("Euromessage", "Euromessage SDK requires min API level 21!");
            return null;
        }

        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://pushs.euromsg.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }
}