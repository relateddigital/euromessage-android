package euromsg.com.euromobileandroid.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionBroadcastReceiver extends BroadcastReceiver {
    private final NotificationActionListener callback;

    public NotificationActionBroadcastReceiver(NotificationActionListener callback) {
        this.callback = callback;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String linkUri = "";
        if (intent != null && "ACTION_CLICK".equals(intent.getAction())) {
            linkUri = intent.getStringExtra("KEY_ACTION_ITEM");
        }

        callback.onNotificationActionClicked(linkUri);
    }
}
