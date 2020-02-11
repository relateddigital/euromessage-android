package euromsg.com.euromobileandroid.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import euromsg.com.euromobileandroid.BuildConfig;
import euromsg.com.euromobileandroid.Constants;


public class EuroLogger {

    public static void debugLog(@NonNull String message) {
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOG_TAG, message);
        }
    }
}
