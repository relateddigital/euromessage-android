package euromsg.com.euromobileandroid.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import java.net.URL;

import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import euromsg.com.euromobileandroid.model.Message;

public final class AppUtils {

    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context) {
        if (sID == null) {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context.getPackageName())
                    == PackageManager.PERMISSION_GRANTED &&
                    pm.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, context.getPackageName())
                            == PackageManager.PERMISSION_GRANTED) {
                try {
                    sID = getIdFromExternalStorage(context);
                } catch (Exception e) {
                    sID = null;
                    e.printStackTrace();
                }
                if(sID == null) {
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
            } else {
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

    public static Bitmap getBitMapFromUri(String photoUrl) {

        URL url;

        Bitmap image = null;
        try {

            AppUtils.setThreadPool();
            url = new URL(photoUrl);
            URLConnection connection = url.openConnection();
            connection.setReadTimeout(30000); // 30 sec

            image = BitmapFactory.decodeStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public static String deviceUDID(Context context) {
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

    public static Intent getLaunchIntent(Context context, Message message) {

        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent notificationIntent = Intent.makeRestartActivityTask(componentName);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("message", message);
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
        } else {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }

    public static String getCurrentDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static String getCurrentTurkeyDateString() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TimeZone tzTurkey = TimeZone.getTimeZone("Turkey");
            dateFormat.setTimeZone(tzTurkey);
            return dateFormat.format(new Date());
        } catch (Exception e) {
            EuroLogger.debugLog("Turkey timezone error : " + e.toString());
            return getCurrentDateString();
        }
    }

    private static String getIdFromExternalStorage(Context context) throws Exception {
        String ID = null;
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            File sdcard = Environment.getExternalStorageDirectory();
            if (!sdcard.exists()) {
                sdcard.mkdirs();
            }
            File dir = new File(sdcard.getAbsolutePath() + "/Download/");
            if(!dir.exists()){
                dir.mkdir();
            }
            File file = new File(dir, "Euromessage");

            if(!file.exists()){
                File installation = new File(context.getFilesDir(), INSTALLATION);
                try {
                    if (installation.exists()) {
                        sID = readInstallationFile(installation);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                file.createNewFile();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    if(sID == null) {
                        fos.write(UUID.randomUUID().toString().getBytes());
                    } else {
                        fos.write(sID.getBytes());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ID = null;
                } finally {
                    fos.close();
                }
            }

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader buff = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line = buff.readLine();
                while(line!=null){
                    sb.append(line);
                    line = buff.readLine();
                }
                try {
                    ID = sb.toString();
                } catch (Exception e){
                    e.printStackTrace();
                    ID = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                ID = null;
            } finally {
                fis.close();
            }
        } else {
            ID = null;
        }
        return ID;
    }

    public static boolean isResourceAvailable(Context context, int resId) {
        if (context != null) {
            try {
                return context.getResources().getResourceName(resId) != null;
            } catch (Resources.NotFoundException ignore) {
            }
        }
        return false;
    }
}