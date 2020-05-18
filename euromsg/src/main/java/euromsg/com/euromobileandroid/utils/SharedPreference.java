package euromsg.com.euromobileandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {

    public static void saveString(Context context, String key, String value) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString(key, value);
        spEditor.apply();
    }

    public static void saveBoolean(Context context, String key,
                                             boolean value) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putBoolean(key, value);
        spEditor.apply();
    }

    public static void saveLong(Context context, String key, long value) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putLong(key, value);
        spEditor.apply();
    }

    public static void saveInt(Context context, String key,
                                   int value) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putInt(key, value);
        spEditor.apply();
    }


    public static boolean hasString(Context context, String key) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName,
                Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    public static int getInt(Context context, String key) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName, Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }

    public static String getString(Context context, String key) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static long getLong(Context context, String key) {
        String appName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(appName,
                Context.MODE_PRIVATE);
        return sp.getLong(key, 0);
    }
}
