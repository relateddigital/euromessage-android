package euromsg.com.euromobileandroid.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.connection.EuroApiService;
import euromsg.com.euromobileandroid.connection.GraylogApiClient;
import euromsg.com.euromobileandroid.connection.RetentionApiClient;
import euromsg.com.euromobileandroid.model.GraylogModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class LogUtils {
    private static final String LOG_TAG = "LogUtils";

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
