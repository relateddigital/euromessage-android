package euromsg.com.euromobileandroid.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class PushNotificationManager {

    private String channelId = "euroChannel";

    public void generateCarouselNotification(Context context, Message pushMessage) {

        ArrayList<Element> elements = pushMessage.getElements();

        CarouselBuilder carouselBuilder = CarouselBuilder.with(context).beginTransaction();
        carouselBuilder.setContentTitle(pushMessage.getTitle()).setContentText(pushMessage.getMessage());

        for (Element item : elements) {
            CarouselItem cItem = new CarouselItem(item.getId(), item.getTitle(), item.getContent(), item.getPicture());
            carouselBuilder.addCarouselItem(cItem);
        }
        carouselBuilder.setOtherRegionClickable(true);
        carouselBuilder.buildCarousel(pushMessage);
    }

    public void generateNotification(Context context, Message pushMessage, Bitmap image) {

        try {

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotificationManager != null) {
                createNotificationChannel(mNotificationManager, channelId, pushMessage.getSound(), context);
            }

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, AppUtils.getLaunchIntent(context, pushMessage), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = createNotificationBuilder(context, image, pushMessage, contentIntent);

            if (pushMessage.getSound() != null) {
                channelId += pushMessage.getSound();
            }

            mNotificationManager.notify(12, mBuilder.build());

        } catch (Exception e) {
            EuroLogger.debugLog("Generate notification : " + e.getMessage());
        }
    }

    public NotificationCompat.Builder createNotificationBuilder(Context context, String contentTitle, String contentText) {

        String title = TextUtils.isEmpty(contentTitle) ? " " : contentTitle;

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                ImageUtils.getAppIcon(context));

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, channelId);
        mBuilder.setContentTitle(title)
                .setContentText(contentText)
                .setLargeIcon(largeIcon)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                .setAutoCancel(true);

        setNotificationSmallIcon(mBuilder, context);

        return mBuilder;
    }

    private NotificationCompat.Builder createNotificationBuilder(Context context,
                                                                 Bitmap pushImage, Message pushMessage, PendingIntent contentIntent) {

        String title = TextUtils.isEmpty(pushMessage.getTitle()) ? AppUtils.getAppLabel(context, "") : pushMessage.getTitle();
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                ImageUtils.getAppIcon(context));

        NotificationCompat.Style style = pushImage == null ?
                new NotificationCompat.BigTextStyle().bigText(pushMessage.getMessage()) :
                new NotificationCompat.BigPictureStyle().bigPicture(pushImage).setSummaryText(pushMessage.getMessage());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                .setStyle(style)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setColorized(false).setAutoCancel(true)
                .setContentText(pushMessage.getMessage());

        setNotificationSmallIcon(mBuilder, context);

        if (pushMessage.getSound() != null) {
            mBuilder.setSound(AppUtils.getSound(context, pushMessage.getSound()));
        } else {
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        mBuilder.setContentIntent(contentIntent);

        return mBuilder;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannel(NotificationManager notificationManager, String channelId, String sound, Context context) {

        int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel notificationChannel = new NotificationChannel(channelId, getChannelName(context), importance);
        notificationChannel.setDescription(getChannelDescription(context));
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        if (sound != null) {
            Uri soundUri = AppUtils.getSound(context, sound);
            AudioAttributes attributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            notificationChannel.setSound(soundUri, attributes);
        }
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private String getChannelDescription(Context context) {
            return AppUtils.getApplicationName(context);
    }

    private String getChannelName(Context context) {
        if (!SharedPreference.getString(context, Constants.CHANNEL_NAME).equals("")) {

            return SharedPreference.getString(context, Constants.CHANNEL_NAME);
        }
        return AppUtils.getApplicationName(context);
    }

    private void setNotificationSmallIcon(NotificationCompat.Builder builder, Context context) {

        int transparentSmallIcon = SharedPreference.getInt(context, Constants.NOTIFICATION_TRANSPARENT_SMALL_ICON);

        if (transparentSmallIcon == 0) {
            transparentSmallIcon = ImageUtils.getAppIcon(context);
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(transparentSmallIcon);
        } else {
            builder.setSmallIcon(transparentSmallIcon);
        }

        if (!SharedPreference.getString(context, Constants.NOTIFICATION_COLOR).equals("")) {
            String color = SharedPreference.getString(context, Constants.NOTIFICATION_COLOR);
            builder.setColor(Color.parseColor(color));
        }
    }
}
