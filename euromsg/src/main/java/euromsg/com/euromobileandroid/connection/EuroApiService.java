package euromsg.com.euromobileandroid.connection;

import euromsg.com.euromobileandroid.model.Retention;
import euromsg.com.euromobileandroid.model.Subscription;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface EuroApiService {

    @Headers({ "Content-Type: application/json;charset=utf-8"})
    @POST("/subscription")
    Call<Void> saveSubscription(@Header("User-Agent") String userAgent, @Body Subscription subscription);

    @Headers({ "Content-Type: application/json;charset=utf-8"})
    @POST("/retention")
    Call<Void> report(@Header("User-Agent") String userAgent, @Body Retention retention);
}
