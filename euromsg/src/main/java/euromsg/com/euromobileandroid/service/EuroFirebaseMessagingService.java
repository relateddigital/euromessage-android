package euromsg.com.euromobileandroid.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;

import euromsg.com.euromobileandroid.carousalnotification.Carousal;
import euromsg.com.euromobileandroid.carousalnotification.CarousalItem;
import euromsg.com.euromobileandroid.connection.ConnectionManager;
import euromsg.com.euromobileandroid.enums.PushType;
import euromsg.com.euromobileandroid.model.CarouselElement;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.Utils;
public class EuroFirebaseMessagingService extends FirebaseMessagingService {

    String carousel  = "{\n" +
            "  \"pushType\": \"Carousel\",\n" +
            "  \"url\": \"https://www.donanimhaber.com/cache/imgdh/20191212190731/31/3/1/0/haber/116815/twitter-artik-jpeg-dosyalarinin-goruntu-kalitesini-bozmayacak116815_0.jpg\",\n" +
            "  \"mediaUrl\": \"https://www.donanimhaber.com/cache/imgdh/20191212190731/31/3/1/0/haber/116815/twitter-artik-jpeg-dosyalarinin-goruntu-kalitesini-bozmayacak116815_0.jpg\",\n" +
            "  \"pushId\": \"770fe20d-5ca1-4b39-8ad3-3aseae24d78a6\",\n" +
            "  \"altUrl\": \"[alturl]\",\n" +
            "  \"sound\": \"ses\",\n" +
            "  \"message\": \"body\",\n" +
            "  \"title\": \"title\",\n" +
            "  \"elements\": [\n" +
            " {\"id\" : \"1\",  \"title\": \"Süper İndirim\",  \"content\": \"Akıllı saatlerde akılalmaz indirimleri kaçırmayın. Harika bir saat\" ,  \"url\": \"http://www.1.com/\" ,  \"picture\": \"https://productimages.hepsiburada.net/s/22/280-413/9986363097138.jpg\"}" +
            " ,{\"id\": \"2\",  \"title\": \"Mükemmel İndirim\",  \"content\": \"Hayatını akıllı saatlerle beraber çok kolaylaştırabilirsin. Yeterki mutlu ol seni çok seviyoruz\" ,  \"url\": \"http://www.2.com/\" ,  \"picture\": \"https://productimages.hepsiburada.net/s/32/400-592/10352550510642.jpg\"}" +
            " ,{\"id\": \"3\",  \"title\": \"Şaşırtıcı İndirim\",  \"content\": \"Mutlu ol çok \" ,  \"url\" : \"http://www.3.com/\",  \"picture\": \"https://productimages.hepsiburada.net/s/32/400-592/10352551526450.jpg\"}" +
            "  ]\n" +
            "}";

    @Override
    public void onNewToken(String token) {
        try {
            EuroLogger.debugLog("On new token : " + token);
            EuroMobileManager.getInstance().subscribe(token, this);
        } catch (Exception e) {
            EuroLogger.debugLog(e.getMessage());
            EuroLogger.debugLog("Failed to complete token refresh");
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        Message pushMessage = new Gson().fromJson(carousel, Message.class);

        // Message pushMessage = new Message(data);
        EuroLogger.debugLog("Message received : " + pushMessage.getMessage());
        if (!TextUtils.isEmpty(pushMessage.getMessage())) {
            if (pushMessage.getPushType() == PushType.Image) {
                generateNotification(getApplicationContext(), data, ConnectionManager.getInstance().getBitmap(pushMessage.getMediaUrl()));
            } else if (pushMessage.getPushType() == PushType.Carousel){
                generateCarouselNotification(pushMessage);
            }else {
                generateNotification(getApplicationContext(), data, null);
            }
        }

        String key = Utils.getPrefString(this, Constants.APPLICATION_KEY);

        EuroMobileManager.sharedManager(key, this).reportReceived(pushMessage.getPushId());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void createNotificationChannel(NotificationManager notificationManager, String channelId) {

        CharSequence name = "Euro Message Channel";
        String description = "Channel for Euro Message notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel = new NotificationChannel(channelId, name, importance);
        notificationChannel.setDescription(description);
        //notificationChannel.enableLights(true);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        //notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void generateNotification(Context context, Map<String, String> data, Bitmap image) {
        try {
            String channelId = "euroChannel";
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotificationManager != null) {
                //Starting with O notification must have a notification channel to work properly
                createNotificationChannel(mNotificationManager, channelId);
            }
            Message pushMessage = new Message(data);
            String title = TextUtils.isEmpty(pushMessage.getTitle()) ? Utils.getAppLabel(getApplicationContext(), "") : pushMessage.getTitle();
            // find and start the launcher activity
            PackageManager packageManager = context.getPackageManager();
            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            final int appIconResId = applicationInfo.icon;
            Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent notificationIntent = Intent.makeRestartActivityTask(componentName);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Set<Map.Entry<String, String>> entrySet = data.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                notificationIntent.putExtra(entry.getKey(), entry.getValue());
            }
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                    appIconResId);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Style style = image == null ?
                    new NotificationCompat.BigTextStyle().bigText(pushMessage.getMessage()) :
                    new NotificationCompat.BigPictureStyle().bigPicture(image).setSummaryText(pushMessage.getMessage());
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                    .setSmallIcon(appIconResId)
                    .setAutoCancel(true)
                    .setStyle(style).setLargeIcon(icon)
                    .setContentTitle(title).setColorized(false)
                    .setContentText(pushMessage.getMessage());
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(12, mBuilder.build());
        } catch (Exception e) {
            EuroLogger.debugLog("Generate notification : " + e.getMessage());
        }
    }


    private void generateCarouselNotification(Message pushMessage) {

        ArrayList<CarouselElement> carouselElements =  pushMessage.getElements();
        Carousal carousal = Carousal.with(this).beginTransaction();
        carousal.setContentTitle(pushMessage.getTitle()).setContentText(pushMessage.getMessage());
        for (CarouselElement item : carouselElements) {
            CarousalItem cItem = new CarousalItem(item.getId(), item.getTitle(), item.getContent(), item.getPicture());
            carousal.addCarousalItem(cItem);
        }
        carousal.setOtherRegionClickable(true);
        carousal.buildCarousal();
    }
}