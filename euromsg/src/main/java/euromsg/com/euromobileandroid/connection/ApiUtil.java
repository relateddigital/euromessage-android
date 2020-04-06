package euromsg.com.euromobileandroid.connection;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import euromsg.com.euromobileandroid.enums.BaseUrl;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.model.Retention;
import euromsg.com.euromobileandroid.model.Subscription;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiUtil {

    private static EuroApiService apiInterface;

    ApiUtil() {
    }

    public static void subscription(Subscription subscription) {

        apiInterface = ApiClient.getClient(BaseUrl.Subscription).create(EuroApiService.class);

        Gson gson = new Gson();

        String s = gson.toJson(subscription);

        Call<Void> call1 = apiInterface.saveSubscription(s);
        call1.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.e("isSuccesful", "msg");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                call.cancel();
            }
        });
    }


    public static void retention(Retention retention) {

        apiInterface = ApiClient.getClient(BaseUrl.Retention).create(EuroApiService.class);

        Gson gson = new Gson();

        String s = gson.toJson(retention);

        Call<Void> call1 = apiInterface.saveSubscription(s);
        call1.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    Log.e("isSuccesful", "msg");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                call.cancel();
            }
        });
    }

}
