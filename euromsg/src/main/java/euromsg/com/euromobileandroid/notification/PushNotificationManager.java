package euromsg.com.euromobileandroid.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.R;
import euromsg.com.euromobileandroid.notification.carousel.CarouselBuilder;
import euromsg.com.euromobileandroid.model.CarouselItem;
import euromsg.com.euromobileandroid.model.Element;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.ImageUtils;
import euromsg.com.euromobileandroid.utils.LogUtils;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class PushNotificationManager {

    Intent intent;


    public void generateCarouselNotification(Context context, Message pushMessage, int notificationId) {

        ArrayList<Element> elements = pushMessage.getElements();

        CarouselBuilder carouselBuilder = CarouselBuilder.with(context, notificationId).beginTransaction();
        carouselBuilder.setContentTitle(pushMessage.getTitle()).setContentText(pushMessage.getMessage());

        for (Element item : elements) {
            CarouselItem cItem = new CarouselItem(item.getId(), item.getTitle(), item.getContent(), item.getPicture());
            carouselBuilder.addCarouselItem(cItem);
        }
        carouselBuilder.buildCarousel(pushMessage);
    }

    public void generateNotification(Context context, Message pushMessage, Bitmap image, int notificationId) {

        try {

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotificationManager != null) {
                createNotificationChannel(mNotificationManager, pushMessage.getSound(), context);
            }

            intent = AppUtils.getStartActivityIntent(context, pushMessage);

            PendingIntent contentIntent;

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            NotificationCompat.Builder mBuilder = createNotificationBuilder(context, image, pushMessage, contentIntent);

            if(mNotificationManager != null) {
                mNotificationManager.notify(notificationId, mBuilder.build());
            }

        } catch (Exception e) {
            EuroLogger.debugLog("Generate notification : " + e.getMessage());
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Creating notification : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
        }
    }

    public NotificationCompat.Builder createNotificationBuilder(Context context, String contentTitle,
                                                                String contentText, Message pushMessage,
                                                                int notificationId) {

        String title = TextUtils.isEmpty(contentTitle) ? " " : contentTitle;

        Bitmap largeIconBitmap;

        boolean willLargeIconBeUsed = SharedPreference.getBoolean(context, Constants.NOTIFICATION_USE_LARGE_ICON);
        if(willLargeIconBeUsed) {
            int largeIcon;

            if(isInDarkMode(context)) {
                largeIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_LARGE_ICON_DARK_MODE);
                if (largeIcon == 0 || !AppUtils.isResourceAvailable(context, largeIcon)) {
                    largeIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_LARGE_ICON);
                }
            } else {
                largeIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_LARGE_ICON);
            }

            if (largeIcon == 0 || !AppUtils.isResourceAvailable(context, largeIcon)) {
                largeIconBitmap = BitmapFactory.decodeResource(context.getResources(),
                        ImageUtils.getAppIcon(context));
            } else {
                largeIconBitmap = BitmapFactory.decodeResource(context.getResources(),
                        largeIcon);
            }
        } else {
            largeIconBitmap = null;
        }

        intent = AppUtils.getStartActivityIntent(context, pushMessage);

        PendingIntent contentIntent;

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String priority = SharedPreference.getString(context, Constants.NOTIFICATION_PRIORITY_KEY);
        int importance;

        if(priority.equals("high")) {
            importance = NotificationCompat.PRIORITY_HIGH;
        } else if(priority.equals("low")){
            importance = NotificationCompat.PRIORITY_LOW;
        } else {
            importance = NotificationCompat.PRIORITY_DEFAULT;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, AppUtils.getNotificationChannelId(context, false));
        mBuilder.setContentTitle(title)
                .setContentText(contentText)
                .setLargeIcon(largeIconBitmap)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.FLAG_SHOW_LIGHTS)
                .setPriority(importance)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);
        setNumber(mBuilder, context);
        setNotificationSmallIcon(mBuilder, context);

        return mBuilder;
    }

    private NotificationCompat.Builder createNotificationBuilder(Context context,
                                                                 Bitmap pushImage, Message pushMessage, PendingIntent contentIntent) {

        String title = TextUtils.isEmpty(pushMessage.getTitle()) ? AppUtils.getAppLabel(context, "") : pushMessage.getTitle();

        Bitmap largeIconBitmap;

        boolean willLargeIconBeUsed = SharedPreference.getBoolean(context, Constants.NOTIFICATION_USE_LARGE_ICON);
        if(willLargeIconBeUsed) {
            int largeIcon;

            if(isInDarkMode(context)) {
                largeIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_LARGE_ICON_DARK_MODE);
                if (largeIcon == 0 || !AppUtils.isResourceAvailable(context, largeIcon)) {
                    largeIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_LARGE_ICON);
                }
            } else {
                largeIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_LARGE_ICON);
            }

            if (largeIcon == 0 || !AppUtils.isResourceAvailable(context, largeIcon)) {
                largeIconBitmap = BitmapFactory.decodeResource(context.getResources(),
                        ImageUtils.getAppIcon(context));
            } else {
                largeIconBitmap = BitmapFactory.decodeResource(context.getResources(),
                        largeIcon);
            }
        } else {
            largeIconBitmap = null;
        }

        String priority = SharedPreference.getString(context, Constants.NOTIFICATION_PRIORITY_KEY);
        int importance;

        if(priority.equals("high")) {
            importance = NotificationCompat.PRIORITY_HIGH;
        } else if(priority.equals("low")){
            importance = NotificationCompat.PRIORITY_LOW;
        } else {
            importance = NotificationCompat.PRIORITY_DEFAULT;
        }

        NotificationCompat.Style style = pushImage == null ?
                new NotificationCompat.BigTextStyle().bigText(pushMessage.getMessage()) :
                new NotificationCompat.BigPictureStyle().bigPicture(pushImage).setSummaryText(pushMessage.getMessage());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, AppUtils.getNotificationChannelId(context, false))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setStyle(style)
                .setLargeIcon(largeIconBitmap)
                .setContentTitle(title)
                .setColorized(false)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.FLAG_SHOW_LIGHTS)
                .setPriority(importance)
                .setContentText(pushMessage.getMessage());

        setNumber(mBuilder, context);

        setNotificationSmallIcon(mBuilder, context);

        if (pushMessage.getSound() != null) {
            mBuilder.setSound(AppUtils.getSound(context, pushMessage.getSound()));
        } else {
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        mBuilder.setContentIntent(contentIntent);

        // TODO : Check the number of buttons and related
        // pending intents here when BE gets ready and
        // set them accordingly.
        /*
        mBuilder.addAction(R.drawable.notification_button, "Open" , contentIntent);
        */

        return mBuilder;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void createNotificationChannel(NotificationManager notificationManager, String sound, Context context) {

        String priority = SharedPreference.getString(context, Constants.NOTIFICATION_PRIORITY_KEY);
        int importance;

        if(priority.equals("high")) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        } else if(priority.equals("low")){
            importance = NotificationManager.IMPORTANCE_LOW;
        } else {
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        }

        NotificationChannel notificationChannel = new NotificationChannel(AppUtils.getNotificationChannelId(context, false), getChannelName(context), importance);
        notificationChannel.setDescription(getChannelDescription(context));
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);

        if (sound != null) {
            Uri soundUri = AppUtils.getSound(context, sound);
            AudioAttributes attributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            notificationChannel.setSound(soundUri, attributes);
        }
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public static String getChannelDescription(Context context) {
        return AppUtils.getApplicationName(context);
    }

    public static String getChannelName(Context context) {
        if (!SharedPreference.getString(context, Constants.CHANNEL_NAME).equals("")) {

            return SharedPreference.getString(context, Constants.CHANNEL_NAME);
        }
        return AppUtils.getApplicationName(context);
    }

    private void setNumber(NotificationCompat.Builder mBuilder, Context context) {
        if (SharedPreference.getInt(context, Constants.BADGE) == Constants.ACTIVE) {
            mBuilder.setNumber(1).setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
        }
    }

    private void setNotificationSmallIcon(NotificationCompat.Builder builder, Context context) {

        int transparentSmallIcon;

        if(isInDarkMode(context)) {
            transparentSmallIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON_DARK_MODE);
            if (transparentSmallIcon == 0 || !AppUtils.isResourceAvailable(context, transparentSmallIcon)) {
                transparentSmallIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON);
            }
        } else {
            transparentSmallIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON);
        }

        if (transparentSmallIcon == 0 || !AppUtils.isResourceAvailable(context, transparentSmallIcon)) {
            transparentSmallIcon = ImageUtils.getAppIcon(context);
        }

        builder.setSmallIcon(transparentSmallIcon);

        if (!SharedPreference.getString(context, Constants.NOTIFICATION_COLOR).equals("")) {
            String color = SharedPreference.getString(context, Constants.NOTIFICATION_COLOR);
            builder.setColor(Color.parseColor(color));
        }
    }

    private Boolean isInDarkMode(Context context) {
        return context.getResources().getString(R.string.mode).equals("Night");
    }
}
