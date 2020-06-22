package euromsg.com.euromobileandroid.connection;

import java.util.concurrent.TimeUnit;

import euromsg.com.euromobileandroid.connection.interceptor.RawResponseInterceptor;
import euromsg.com.euromobileandroid.enums.BaseUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetentionApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(new RawResponseInterceptor())
                        .addInterceptor(interceptor).connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES);

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://pushr.euromsg.com/")
                    .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build())
                    .build();
        }

        return retrofit;
    }
}