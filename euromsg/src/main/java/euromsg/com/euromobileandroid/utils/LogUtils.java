package euromsg.com.euromobileandroid.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.connection.EuroApiService;
import euromsg.com.euromobileandroid.connection.GraylogApiClient;
import euromsg.com.euromobileandroid.model.GraylogModel;
import euromsg.com.euromobileandroid.utils.LogConfigApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

import retrofit2.Retrofit;
import retrofit2.http.GET;

public final class LogUtils {
    private static final String LOG_TAG = "LogUtils";

    // Private constructor to prevent instantiation
    private LogUtils() {}

    /**
     * Retrofit API interface for fetching log configuration.
     * Equivalent to the Kotlin LogConfigApi interface nested within LogUtils.
     */
    public interface LogConfigApi {
        @GET("log_rc.json")
        Call<LogConfig> getLogConfig();
    }

    /**
     * Determines whether a log should be sent based on remote configuration and customer ID.
     * IMPORTANT: This method performs a synchronous network call (call.execute()).
     * It MUST NOT be called on the main UI thread in Android.
     *
     * @param context The application context.
     * @return true if the log should be sent, false otherwise.
     */
    private static boolean shouldSendLog(Context context) {
        try {
            Retrofit retrofitClient = LogConfigApiClient.getClient();
            if (retrofitClient == null) {
                Log.w(LOG_TAG, "LogConfigApiClient.client is null. Defaulting to send log.");
                return true;
            }
            LogConfigApi apiService = retrofitClient.create(LogConfigApi.class);

            Call<LogConfig> call = apiService.getLogConfig();
            if (call == null) {
                Log.w(LOG_TAG, "Failed to create call for getLogConfig. Defaulting to send log.");
                return true;
            }

            // Synchronous network call - MUST be on a background thread.
            Response<LogConfig> response = call.execute(); // Can throw IOException

            LogConfig config = null;
            if (response != null && response.isSuccessful()) {
                config = response.body();
            }

            if (config == null) {
                Log.w(LOG_TAG, "Failed to retrieve config, response was null, not successful, or body was null. Defaulting to send log.");
                return true; // Config yoksa default true
            }

            if (!config.isLoggingEnabled()) {
                return false;
            }


            String customerId = EuroMobileManager.getInstance().subscription.getAppAlias();

            if (customerId == null || customerId.isEmpty()) {
                Log.i(LOG_TAG, "Customer ID could not be determined. Defaulting to send log.");
                return true;
            }

            Log.i(LOG_TAG, "Customer ID: " + customerId);

            if (config.getExcludedCustomerIds() == null) {
                Log.w(LOG_TAG, "Excluded customer IDs list is null. Assuming customer is not excluded (sending log).");
                return true; // Or false, based on how you want to handle missing exclusion list
            }

            return !config.getExcludedCustomerIds().contains(customerId);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException during fetching log config: " + e.getMessage(), e);
            return true; // Hata durumunda default olarak log gönder
        } catch (Exception e) { // Catch other potential runtime exceptions
            Log.e(LOG_TAG, "Failed to fetch log config due to an unexpected error: " + e.getMessage(), e);
            return true; // Hata durumunda default olarak log gönder
        }
    }

    public static void sendGraylogMessage(GraylogModel graylogModel) {
        if(GraylogApiClient.getClient() == null) {
            Log.e(LOG_TAG, "Euromessage SDK requires min API level 21!");
            return;
        }
        EuroApiService apiService = GraylogApiClient.getClient().create(EuroApiService.class);
        Call<Void> call = apiService.sendLogToGraylog(graylogModel);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(LOG_TAG, "Sending the graylog message is successful");
                } else {
                    Log.i(LOG_TAG, "Sending the graylog message is failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.i(LOG_TAG, "Sending the graylog message is failed");
                t.printStackTrace();
            }
        });
    }

    public static void formGraylogModel(Context context, String logLevel, String logMessage,
                                        String logPlace){
        if (!shouldSendLog(context)) {
            Log.i(LOG_TAG, "Log sending skipped due to configuration.");
            return;
        }
        EuroMobileManager euroMobileManager;
        if(EuroMobileManager.getInstance() == null) {
            String appAlias = SharedPreference.getString(context, Constants.GOOGLE_APP_ALIAS);
            String huaweiAppAlias = SharedPreference.getString(context, Constants.HUAWEI_APP_ALIAS);

            euroMobileManager = EuroMobileManager.init(appAlias, huaweiAppAlias, context);
        } else {
            euroMobileManager = EuroMobileManager.getInstance();
        }
        euroMobileManager.sendLogToGraylog(logLevel, logMessage, logPlace);
    }
}
