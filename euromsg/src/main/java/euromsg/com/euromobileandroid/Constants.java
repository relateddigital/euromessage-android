package euromsg.com.euromobileandroid;

public class Constants {

    //https://commonsware.com/blog/2020/10/14/android-studio-4p1-library-modules-version-code.html
    static final String SDK_VERSION = euromsg.com.euromobileandroid.BuildConfig.VERSION_NAME;


    public static final String EURO_CONSENT_TIME_KEY = "ConsentTime";
    static final String EURO_CONSENT_SOURCE_KEY = "ConsentSource";
    static final String EURO_CONSENT_SOURCE_VALUE = "HS_MOBIL";
    static final String EURO_RECIPIENT_TYPE_KEY = "RecipientType";
    static final String EURO_RECIPIENT_TYPE_BIREYSEL = "BIREYSEL";
    static final String EURO_RECIPIENT_TYPE_TACIR = "TACIR";
    public static final String TOKEN_KEY = "token-key";

    static final String EURO_EMAIL_KEY = "email";
    static final String EURO_TWITTER_KEY = "twitter";
    static final String EURO_FACEBOOK_KEY = "facebook";
    static final String EURO_MSISDN_KEY = "msisdn";
    static final String EURO_USER_KEY = "keyID";
    static final String EURO_LOCATION_KEY = "location";
    static final String EURO_SUBSCRIPTION_KEY = "subscription";
    static final String ALREADY_SENT_SUBSCRIPTION_JSON = "sent_subscription";
    static final String LAST_SUBSCRIPTION_TIME = "last_subscription_time";

    public static final String LOG_TAG = "EuroMessage";
    public static final String HUAWEI_APP_ALIAS = "AppKeyHuwaei";
    public static final String GOOGLE_APP_ALIAS = "AppKeyGoogle";

    public static final String  NOTIFICATION_TRANSPARENT_SMALL_ICON = "small_icon";
    public static final String  NOTIFICATION_TRANSPARENT_SMALL_ICON_DARK_MODE = "small_icon_dark_mode";
    public static final String  NOTIFICATION_LARGE_ICON = "large_icon";
    public static final String  NOTIFICATION_LARGE_ICON_DARK_MODE = "large_icon_dark_mode";
    public static final String  NOTIFICATION_USE_LARGE_ICON = "use_large_icon";
    public static final String  NOTIFICATION_COLOR = "notification_color";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String BADGE = "badge";
    public static final int ACTIVE = 1;
    public static final int PASSIVE = 0;

    public static final int EVENT_LEFT_ARROW_CLICKED = 1;
    public static final int EVENT_RIGHT_ARROW_CLICKED = 2;
    public static final int EVENT_LEFT_ITEM_CLICKED = 3;
    public static final int EVENT_RIGHT_ITEM_CLICKED = 4;

    public static final String NOTIFICATION_ID = "NotificationId";

    public static final String EVENT_CAROUSAL_ITEM_CLICKED_KEY = "CarouselItemClicked";
    public static final String CAROUSAL_IMAGE_BEGENNING = "CarouselImage";
    public static final String CAROUSAL_EVENT_FIRED_INTENT_FILTER = "CAROUSALNOTIFICATIONFIRED";
    public static final String CAROUSAL_ITEM_CLICKED_INTENT_FILTER = "CarouselItemClickIntentFilter";
    public static final String CAROUSAL_ITEM_CLICKED_KEY = "CarouselItemClickedKey";
    public static final String CAROUSEL_ITEM_CLICKED_URL = "CarouselItemClickedUrl";
    public static final String CAROUSAL_SET_UP_KEY = "CAROUSAL_SET_UP_KEY";
    public static final String CAROUSAL_SMALL_ICON_FILE_NAME = "smallIconCarousel";
    public static final String CAROUSAL_LARGE_ICON_FILE_NAME = "largeIconCarousel";
    public static final String CAROUSAL_PLACEHOLDER_ICON_FILE_NAME = "placeHolderIconCarousel";

    public static final String EURO_SUBSCRIPTION_NO_EMAIL_KEY = "subscription_no_email";
    public static final String EURO_SUBSCRIPTION_DATE_KEY = "subscription_date";
    public static final String EURO_SUBSCRIPTION_WITH_EMAIL_KEY = "subscription_with_email";
    public static final String EURO_SUBSCRIPTION_DATE_WITH_EMAIL_KEY = "subscription_date_with_email";

    public static final String INTENT_NAME = "intent_name";
    public static final int UI_FEATURES_MIN_API = 21;

    public static final String PAYLOAD_SP_KEY = "payload_sp";
    public static final String PAYLOAD_SP_ARRAY_KEY = "messages";
    public static final String PAYLOAD_SP_ID_KEY = "payload_sp_with_id";
    public static final String PAYLOAD_SP_ARRAY_ID_KEY = "messages_with_id";

    public static final String NOTIFICATION_CHANNEL_ID_KEY = "not_channel_id_key";
    public static final String NOTIFICATION_CHANNEL_NAME_KEY = "not_channel_name_key";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION_KEY = "not_channel_description_key";
    public static final String NOTIFICATION_CHANNEL_SOUND_KEY = "not_channel_sound_key";
    public static final String NOTIFICATION_PRIORITY_KEY = "not_priority_key";

    public static final String DEFAULT_ANDROID_SOUND = "default_android_sound";
    public static final String NOTIFICATION_LOGIN_ID_KEY = "notification_login_id_key";
}