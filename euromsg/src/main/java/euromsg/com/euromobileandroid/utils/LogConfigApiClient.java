package euromsg.com.euromobileandroid.utils;

import euromsg.com.euromobileandroid.Constants;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Provides a Retrofit client instance for LogConfig API.
 * Equivalent to the Kotlin object LogConfigApiClient.
 */
public final class LogConfigApiClient {

    private static volatile Retrofit retrofit = null;

    // Private constructor to prevent instantiation
    private LogConfigApiClient() {}

    /**
     * Gets the Retrofit client instance.
     * Uses double-checked locking for thread-safe lazy initialization.
     * @return The Retrofit client instance.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (LogConfigApiClient.class) {
                if (retrofit == null) {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(5, TimeUnit.SECONDS)
                            .writeTimeout(5, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl("https://mbls.visilabs.net/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .build();
                }
            }
        }
        return retrofit;
    }
}
