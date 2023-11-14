package euromsg.com.euromobileandroid.callback;

public interface PushNotificationPermissionInterface {

    void success(boolean granted);

    void fail(String errorMessage);
}
