package euromsg.com.euromobileandroid.service;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;

import euromsg.com.euromobileandroid.connection.ConnectionManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class EuroFirebaseMessagingService extends FirebaseMessagingService {

    String carousel = "{\n" +
            "  \"pushType\": \"Carousel\",\n" +
            "  \"url\": \"https://www.donanimhaber.com/cache/imgdh/20191212190731/31/3/1/0/haber/116815/twitter-artik-jpeg-dosyalarinin-goruntu-kalitesini-bozmayacak116815_0.jpg\",\n" +
            "  \"mediaUrl\": \"https://www.donanimhaber.com/cache/imgdh/20191212190731/31/3/1/0/haber/116815/twitter-artik-jpeg-dosyalarinin-goruntu-kalitesini-bozmayacak116815_0.jpg\",\n" +
            "  \"pushId\": \"770fe20d-5ca1-4b39-8ad3-3aseae24d78a6\",\n" +
            "  \"altUrl\": \"[alturl]\",\n" +
            "  \"sound\": \"ses\",\n" +
            "  \"message\": \"Harika indirim detayları\",\n" +
            "  \"title\": \"Dikkat dikkat\",\n" +
            "  \"elements\": [\n" +
            " {\"id\" : \"1\",  \"title\": \"Süper İndirim\",  \"content\": \"Akıllı saatlerde akılalmaz indirimleri kaçırmayın. Harika bir saat\" ,  \"url\": \"http://www.1.com/\" ,  \"picture\": \"https://productimages.hepsiburada.net/s/22/280-413/9986363097138.jpg\"}" +
            " ,{\"id\": \"2\",  \"title\": \"Mükemmel İndirim\",  \"content\": \"Hayatını akıllı saatlerle beraber çok kolaylaştırabilirsin. Yeterki mutlu ol seni çok seviyoruz\" ,  \"url\": \"http://www.2.com/\" ,  \"picture\": \"https://productimages.hepsiburada.net/s/32/400-592/10352550510642.jpg\"}" +
            " ,{\"id\": \"3\",  \"title\": \"Şaşırtıcı İndirim\",  \"content\": \"Mutlu ol çok \" ,  \"url\" : \"http://www.3.com/\",  \"picture\": \"https://productimages.hepsiburada.net/s/32/400-592/10352551526450.jpg\"}" +
            "  ]\n" +
            "}";

    @Override
    public void onNewToken(@NonNull String token) {
        try {
            EuroLogger.debugLog("On new token : " + token);
            EuroMobileManager.getInstance().subscribe(token, this);
        } catch (Exception e) {
            EuroLogger.debugLog(e.toString());
            EuroLogger.debugLog("Failed to complete token refresh");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        PushNotificationManager pushNotificationManager = new PushNotificationManager();

        Map<String, String> remoteMessageData = remoteMessage.getData();

        Message pushMessage = new Gson().fromJson(carousel, Message.class);
        //Message pushMessage = new Message(remoteMessageData);

        EuroLogger.debugLog("Message received : " + pushMessage.getMessage());

        switch (pushMessage.getPushType()) {

            case Carousel:
                pushNotificationManager.generateCarouselNotification(this, pushMessage);

                break;

            case Image:
                pushNotificationManager.generateNotification(this, remoteMessageData, ConnectionManager.getInstance().getBitMapFromUri(pushMessage.getMediaUrl()));

                break;

            case Text:
                pushNotificationManager.generateNotification(this, remoteMessageData, null);

                break;
        }

        String applicationKey = SharedPreference.getString(this, Constants.APPLICATION_KEY);

        EuroMobileManager.createInstance(applicationKey, this).reportReceived(pushMessage.getPushId());
    }
}