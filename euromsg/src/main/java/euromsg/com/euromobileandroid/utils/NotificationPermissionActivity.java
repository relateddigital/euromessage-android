package euromsg.com.euromobileandroid.utils;

import androidx.activity.ComponentActivity;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.NotificationManagerCompat;

import android.os.Bundle;

public class NotificationPermissionActivity extends ComponentActivity {
    public static NotificationPermissionCallback callback;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                callback.onPermissionResult(isGranted);
                finish();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        String permission = "android.permission.POST_NOTIFICATIONS";
        boolean granted = NotificationManagerCompat.from(this).areNotificationsEnabled();
        if (!granted) {
            requestPermissionLauncher.launch(permission);
        } else {
            callback.onPermissionResult(granted);
            finish();
        }
    }
}

