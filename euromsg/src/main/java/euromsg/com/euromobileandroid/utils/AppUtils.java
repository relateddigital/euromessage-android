package euromsg.com.euromobileandroid.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AppUtils {

    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(installation, "r");
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            return new String(bytes);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    Log.e("Error", e.toString());
                }
            }
        }
    }

    private static void writeInstallationFile(File installation)
            throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

    public static String appVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
            EuroLogger.debugLog("Version Name Error : " + e.toString());

        }
        return null;
    }

    @SuppressLint("WrongConstant")
    private static boolean hasReadPhoneStatePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private static void askForReadPhoneStatePermission(Fragment fragment, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasReadPhoneStatePermission(fragment.getContext())) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fragment.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, requestCode);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public static String deviceUDID(Context context) {

        try {
            if (hasReadPhoneStatePermission(context)) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (hasReadPhoneStatePermission(context)) {
                    try {
                        if (tm.getDeviceId() != null) {
                            return tm.getDeviceId(); // unique identifier from phone
                        } else {
                            return Secure.getString(context.getContentResolver(),
                                    Secure.ANDROID_ID); // if device id not available get OS
                            // id
                        }
                    } catch (SecurityException security) {
                        return "";
                    }
                } else {
                    return "";
                }
            } else {
                askForReadPhoneStatePermission(null, 1200);
            }
        } catch (Exception e) {
            //TODO Log here
        }
        return id(context);
    }

    public static String osVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String osType() {
        return "Android";
    }

    public static String deviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String carrier(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperator();
    }

    public static String local(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    public static String deviceType() {
        return android.os.Build.MANUFACTURER + " : " + android.os.Build.MODEL;
    }

    public static String getAppLabel(Context pContext, String defaultText) {
        PackageManager lPackageManager = pContext.getPackageManager();
        ApplicationInfo lApplicationInfo = null;
        try {
            lApplicationInfo = lPackageManager.getApplicationInfo(pContext.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String) (lApplicationInfo != null ? lPackageManager.getApplicationLabel(lApplicationInfo) : defaultText);
    }

    public static Intent getLaunchIntent(Context context, Map<String, String> data) {

        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent notificationIntent = Intent.makeRestartActivityTask(componentName);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (data != null) {
            Set<Map.Entry<String, String>> entrySet = data.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                notificationIntent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        return notificationIntent;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static void setThreadPool() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static Uri getSound(Context context, String sound) {
        int id = context.getResources().getIdentifier(sound, "raw", context.getPackageName());
        if (id != 0) {
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + id);
        }else{
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }
}