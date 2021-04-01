package euromsg.com.euromobileandroid.notification.carousel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.io.Serializable;
import java.util.ArrayList;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.R;
import euromsg.com.euromobileandroid.connection.CarouselImageDownloaderManager;
import euromsg.com.euromobileandroid.model.CarouselItem;
import euromsg.com.euromobileandroid.model.Carousel;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.ImageUtils;

public class CarouselBuilder implements Serializable {

    private static CarouselBuilder carouselBuilder;
    private Context context;
    private ArrayList<CarouselItem> carouselItems;
    private String contentTitle, contentText; //title and text while it is small
    private String bigContentTitle, bigContentText; //title and text when it becomes large
    private String leftItemTitle, leftItemDescription;
    private String rightItemTitle, rightItemDescription;
    private final String channelId = "euroChannel";

    Message message;
    private static final String TAG = "Carousel";
    private NotificationCompat.Builder mBuilder;
    private int carouselNotificationId = 9873715; //Random id for notification. Will cancel any notification that have existing same id.

    private static int currentStartIndex = 0; //Variable that keeps track of where the startIndex is

    private static Bitmap appIcon;
    private static Bitmap smallIcon;
    private static int smallIconResourceId = -1; //check before setting it that it does exists
    private static Bitmap largeIcon;
    private static Bitmap caraousalPlaceholder;

    private CarouselItem leftItem, rightItem;
    private Bitmap leftItemBitmap, rightItemBitmap;

    private Carousel carousel;
    private String smallIconPath, largeIconPath, placeHolderImagePath; //Stores path of these images if set by user

    private boolean isOtherRegionClickable = false;
    private boolean isImagesInCarousel = true;

    private static final String CAROUSAL_ITEM_CLICKED_KEY = "CarouselNotificationItemClickedKey";

    private CarouselBuilder(Context context, int notificationId) {
        this.context = context;
        this.carouselNotificationId = notificationId;
        String channelId = "euroChannel";
        mBuilder = new NotificationCompat.Builder(context, channelId);
    }

    public static CarouselBuilder with(Context context, int notificationId) {
        if (carouselBuilder == null) {
            synchronized (CarouselBuilder.class) {
                if (carouselBuilder == null) {
                    carouselBuilder = new CarouselBuilder(context, notificationId);
                    try {
                        appIcon = ImageUtils.drawableToBitmap(context.getPackageManager().getApplicationIcon(context.getPackageName()));
                    } catch (PackageManager.NameNotFoundException e) {
                        appIcon = null;
                        Log.e(TAG, "Unable to retrieve app Icon");
                    }
                }
            }
        }
        return carouselBuilder;
    }

    public CarouselBuilder beginTransaction() {
        clearCarouselIfExists();
        return this;
    }

    public void addCarouselItem(CarouselItem carouselItem) {
        if (carouselItem != null) {
            if (carouselItems == null) {
                carouselItems = new ArrayList<>();
            }
            carouselItems.add(carouselItem);
        } else {
            Log.e(TAG, "Null carousel can't be added!");
        }
    }

    public CarouselBuilder setContentTitle(String title) {
        if (title != null) {
            this.contentTitle = title;
        } else {
            Log.e(TAG, "Null parameter");
        }
        return this;
    }

    public void setContentText(String contentText) {
        if (contentText != null) {
            this.contentText = contentText;
        } else {
            Log.e(TAG, "Null parameter");
        }
    }

    public void setBigContentText(String bigContentText) {
        if (bigContentText != null) {
            this.bigContentText = bigContentText;
        } else {
            Log.e(TAG, "Null parameter");
        }
    }

    public void setBigContentTitle(String bigContentTitle) {
        if (bigContentTitle != null) {
            this.bigContentTitle = bigContentTitle;
        } else {
            Log.e(TAG, "Null parameter");
        }
    }

    public CarouselBuilder setNotificationPriority(int priority) {
        if (priority >= NotificationCompat.PRIORITY_MIN && priority <= NotificationCompat.PRIORITY_MAX) {
        } else {
            Log.i(TAG, "Invalid priority");
        }
        return this;
    }

    public CarouselBuilder setSmallIconResource(int resourceId) {
        try {
            smallIcon = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            smallIcon = null;
            Log.e(TAG, "Unable to decode resource");
        }

        if (smallIcon != null) {  //meaning a valid resource
            smallIconResourceId = resourceId;
        }
        return this;
    }

    public CarouselBuilder setLargeIcon(int resourceId) {
        try {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            Log.e(TAG, "Unable to decode resource");
        }
        return this;
    }

    public CarouselBuilder setLargeIcon(Bitmap large) {
        if (large != null) {
            largeIcon = large;
        } else {
            largeIcon = null;
            Log.i(TAG, "Null parameter");
        }
        return this;
    }

    public void setCarouselPlaceHolder(int resourceId) {
        try {
            caraousalPlaceholder = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            caraousalPlaceholder = null;
            Log.e(TAG, "Unable to decode resource");
        }
    }

    public void setOtherRegionClickable(boolean isOtherRegionClickable) {
        this.isOtherRegionClickable = isOtherRegionClickable;
    }

    public void buildCarousel(Message message) {

        this.message = message;
        boolean isImagesInCarous = false;
        int numberofImages = 0;
        if (carouselItems != null && carouselItems.size() > 0) {
            for (CarouselItem item : carouselItems) {
                if (!TextUtils.isEmpty(item.getPhotoUrl())) {
                    isImagesInCarous = true;
                    numberofImages++;
                }
            }
            if (isImagesInCarous) {
                CarouselImageDownloaderManager carouselImageDownloaderManager = new CarouselImageDownloaderManager(context, carouselItems
                        , numberofImages, new CarouselImageDownloaderManager.OnDownloadsCompletedListener() {
                    @Override
                    public void onComplete() {
                        initiateCarouselTransaction();
                    }
                });
                carouselImageDownloaderManager.startAllDownloads();
            } else {
                this.isImagesInCarousel = false;
                initiateCarouselTransaction();
            }
        }
    }

    private void initiateCarouselTransaction() {
        currentStartIndex = 0;
        if (carouselItems != null && carouselItems.size() > 0) {
            if (carouselItems.size() == 1) {
                prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), null);
            } else {
                prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), carouselItems.get(currentStartIndex + 1));
            }
        }
    }

    private void prepareVariablesForCarouselAndShow(CarouselItem leftItem, CarouselItem rightItem) {
        if (this.leftItem == null) {
            this.leftItem = new CarouselItem();
        }
        if (this.rightItem == null) {
            this.rightItem = new CarouselItem();
        }
        if (leftItem != null) {
            this.leftItem = leftItem;
            leftItemTitle = leftItem.getTitle();
            leftItemDescription = leftItem.getDescription();
            leftItemBitmap = getCarouselBitmap(leftItem);

        }
        if (rightItem != null) {
            this.rightItem = rightItem;
            rightItemTitle = rightItem.getTitle();
            rightItemDescription = rightItem.getDescription();
            rightItemBitmap = getCarouselBitmap(rightItem);

        }
        showCarousel();
    }

    private void showCarousel() {

        if (carouselItems != null && carouselItems.size() > 0) {

            if (carousel == null || carousel.carouselNotificationId != carouselNotificationId) {
                //First save this set up into a carousel setup item
                carousel = saveCarouselSetUp();
            } else {
                carousel.currentStartIndex = currentStartIndex;
                carousel.leftItem = leftItem;
                carousel.rightItem = rightItem;
            }

            setUpCarouselIcons();
            setUpCarouselTitles();

            RemoteViews bigView = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.carousel_notification_item);

            setUpCarouselVisibilities(bigView);
            setUpCarouselItems(bigView);
            setPendingIntents(bigView);

            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotifyManager != null) {
                PushNotificationManager.createNotificationChannel(mNotifyManager, channelId, message.getSound(), context);
            }

            PushNotificationManager pushNotificationManager = new PushNotificationManager();

            mBuilder = pushNotificationManager.createNotificationBuilder(context, contentTitle, contentText);

            // TODO : Check the number of buttons and related
            // pending intents here when BE gets ready and
            // set them accordingly.
            /*
            mBuilder.addAction(R.drawable.notification_button, "Open" , contentIntent);
            */

            if (isOtherRegionClickable) {
                setOtherRegionClickable();
            }

            Notification foregroundNote = mBuilder.build();
            foregroundNote.bigContentView = bigView;

            if (mNotifyManager != null) {
                mNotifyManager.notify(carouselNotificationId, foregroundNote);
            }
        } else {
            Log.e(TAG, "Empty item array or of length less than 2");
        }

    }

    private void setOtherRegionClickable() {

        Intent carouselIntent = new Intent( Constants.CAROUSAL_EVENT_FIRED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putInt(  Constants.EVENT_CAROUSAL_ITEM_CLICKED_KEY,   Constants.EVENT_OTHER_REGION_CLICKED);
        bundle.putParcelable(  Constants.CAROUSAL_SET_UP_KEY, carousel);
        carouselIntent.putExtras(bundle);
        PendingIntent pIntent = PendingIntent.getBroadcast(context,   Constants.EVENT_OTHER_REGION_CLICKED, carouselIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);
    }

    private Bitmap getCarouselBitmap(CarouselItem item) {
        Bitmap bitmap = null;
        if (item != null) {
            if (!TextUtils.isEmpty(item.getImageFileName()) && !TextUtils.isEmpty(item.getImageFileLocation())) {
                bitmap = ImageUtils.loadImageFromStorage(item.getImageFileLocation(), item.getImageFileName());
                if (bitmap != null)
                    return bitmap;
            }
            if (caraousalPlaceholder != null)
                return caraousalPlaceholder;
            else if (appIcon != null)
                return appIcon;
        }

        return bitmap;
    }

    private void setUpCarouselVisibilities(RemoteViews bigView) {

        if (carouselItems.size() < 3) {
            bigView.setViewVisibility(R.id.iv_arrow_left, View.GONE);
            bigView.setViewVisibility(R.id.iv_arrow_right, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.iv_arrow_left, View.VISIBLE);
            bigView.setViewVisibility(R.id.iv_arrow_right, View.VISIBLE);
        }
        if (carouselItems.size() < 2) {
            bigView.setViewVisibility(R.id.ll_right_item_layout, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ll_right_item_layout, View.VISIBLE);
        }
        if (TextUtils.isEmpty(bigContentText)) {
            bigView.setViewVisibility(R.id.tv_carousel_content, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tv_carousel_content, View.VISIBLE);
        }
        if (TextUtils.isEmpty(bigContentTitle)) {
            bigView.setViewVisibility(R.id.tv_carousel_title, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tv_carousel_title, View.VISIBLE);
        }
        if (TextUtils.isEmpty(leftItemTitle)) {
            bigView.setViewVisibility(R.id.tv_left_title_text, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tv_left_title_text, View.VISIBLE);
        }
        if (TextUtils.isEmpty(leftItemDescription)) {
            bigView.setViewVisibility(R.id.tv_left_description_text, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tv_left_description_text, View.VISIBLE);
        }
        if (TextUtils.isEmpty(rightItemTitle)) {
            bigView.setViewVisibility(R.id.tv_right_title_text, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tv_right_title_text, View.VISIBLE);
        }
        if (TextUtils.isEmpty(rightItemDescription)) {
            bigView.setViewVisibility(R.id.tv_right_description_text, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tv_right_description_text, View.VISIBLE);
        }
        if (!isImagesInCarousel) {
            bigView.setViewVisibility(R.id.iv_image_left, View.GONE);
            bigView.setViewVisibility(R.id.iv_image_right, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.iv_image_left, View.VISIBLE);
            bigView.setViewVisibility(R.id.iv_image_right, View.VISIBLE);
        }

    }

    private void setUpCarouselTitles() {
        if (TextUtils.isEmpty(contentTitle)) {
            setContentTitle("");
        }

        if (bigContentTitle == null)
            bigContentTitle = "";
        if (bigContentText == null)
            bigContentText = "";
    }

    private void setUpCarouselIcons() {
        if (appIcon != null) {
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        } else {
            appIcon = BitmapFactory.decodeResource(context.getResources(), ImageUtils.getAppIcon(context));
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = ImageUtils.getAppIcon(context);
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = R.drawable.ic_carousel_icon;
        }
    }

    private void setUpCarouselItems(RemoteViews bigView) {
        if (leftItemBitmap != null) {
            bigView.setImageViewBitmap(R.id.iv_image_left, leftItemBitmap);
        }
        if (rightItemBitmap != null) {
            bigView.setImageViewBitmap(R.id.iv_image_right, rightItemBitmap);
        }
        bigView.setImageViewBitmap(R.id.iv_carousel_app_icon, largeIcon);
        bigView.setTextViewText(R.id.tv_carousel_title, bigContentTitle);
        bigView.setTextViewText(R.id.tv_carousel_content, bigContentText);
        bigView.setTextViewText(R.id.tv_right_title_text, rightItemTitle);
        bigView.setTextViewText(R.id.tv_right_description_text, rightItemDescription);
        bigView.setTextViewText(R.id.tv_left_title_text, leftItemTitle);
        bigView.setTextViewText(R.id.tv_left_description_text, leftItemDescription);
    }

    private void setPendingIntents(RemoteViews bigView) {
        //right arrow
        PendingIntent rightArrowPendingIntent = getPendingIntent(  Constants.EVENT_RIGHT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.iv_arrow_right, rightArrowPendingIntent);
        //left arrow
        PendingIntent leftArrowPendingIntent = getPendingIntent(  Constants.EVENT_LEFT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.iv_arrow_left, leftArrowPendingIntent);
        //right item
        PendingIntent rightItemPendingIntent = getPendingIntent(  Constants.EVENT_RIGHT_ITEM_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ll_right_item_layout, rightItemPendingIntent);
        //left item
        PendingIntent leftItemPendingIntent = getPendingIntent(  Constants.EVENT_LEFT_ITEM_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ll_left_item_layout, leftItemPendingIntent);
    }

    private PendingIntent getPendingIntent(int eventClicked) {

        Intent carouselIntent = new Intent(context, CarouselEventReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt(  Constants.NOTIFICATION_ID, carouselNotificationId);
        bundle.putInt(  Constants.EVENT_CAROUSAL_ITEM_CLICKED_KEY, eventClicked);
        bundle.putParcelable(  Constants.CAROUSAL_SET_UP_KEY, carousel);
        bundle.putSerializable("message", message);
        carouselIntent.putExtras(bundle);
        return PendingIntent.getBroadcast(context, eventClicked, carouselIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Carousel saveCarouselSetUp() {
        setUpfilePathOfImages();
        return new Carousel(carouselItems, contentTitle, contentText,
                bigContentTitle, bigContentText, carouselNotificationId,
                currentStartIndex, smallIconPath, smallIconResourceId, largeIconPath,
                placeHolderImagePath, leftItem, rightItem, isOtherRegionClickable, isImagesInCarousel);
    }

    private void setUpfilePathOfImages() {
        if (smallIcon != null) {
            smallIconPath = ImageUtils.saveBitmapToInternalStorage(context, smallIcon,
                      Constants.CAROUSAL_SMALL_ICON_FILE_NAME);
        }
        if (largeIcon != null) {
            largeIconPath = ImageUtils.saveBitmapToInternalStorage(context, largeIcon,
                      Constants.CAROUSAL_LARGE_ICON_FILE_NAME);
        }
        if (caraousalPlaceholder != null) {
            placeHolderImagePath = ImageUtils.saveBitmapToInternalStorage(context, caraousalPlaceholder,
                      Constants.CAROUSAL_PLACEHOLDER_ICON_FILE_NAME);
        }
    }

    private void clearCarouselIfExists() {
        if (carouselItems != null) {
            /*for (CarouselItem cr : carouselItems) {
                if (cr.getImageFileName() != null)
                if(context.deleteFile(cr.getImageFileName()))
                    Log.i(TAG, "Image deleted.");
            }*/
            carouselItems.clear();

            smallIconResourceId = -1;
            isOtherRegionClickable = false;
            isImagesInCarousel = true;
            smallIcon = null;
            smallIconPath = null;
            largeIcon = null;
            placeHolderImagePath = null;
            caraousalPlaceholder = null;
            contentText = null;
            contentTitle = null;
            bigContentText = null;
            bigContentTitle = null;

            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotifyManager != null) {
                mNotifyManager.cancel(carouselNotificationId);
            }
        }
        //ToDo :  delete all cache files
    }

    void handleClickEvent(int clickEvent, Carousel setUp) {

          verifyAndSetUpVariables(setUp);

        switch (clickEvent) {
            case   Constants.EVENT_LEFT_ARROW_CLICKED:
                onLeftArrowClicked();
                break;
            case   Constants.EVENT_RIGHT_ARROW_CLICKED:
                onRightArrowClicked();
                break;
            case   Constants.EVENT_LEFT_ITEM_CLICKED:
                onLeftItemClicked();
                break;
            case   Constants.EVENT_RIGHT_ITEM_CLICKED:
                onRightItemClicked();
                break;
            case   Constants.EVENT_OTHER_REGION_CLICKED:
                onOtherRegionClicked ();
                break;
            default:
                break;
        }
    }

    private void verifyAndSetUpVariables(Carousel setUp) {

        if (carousel == null) {

            carouselItems = setUp.carouselItems;
            contentTitle = setUp.contentTitle;
            contentText = setUp.contentText;
            bigContentTitle = setUp.bigContentTitle;
            bigContentText = setUp.bigContentText;
            carouselNotificationId = setUp.carouselNotificationId;
            currentStartIndex = setUp.currentStartIndex;
            smallIconPath = setUp.smallIcon;
            largeIconPath = setUp.largeIcon;
            placeHolderImagePath = setUp.caraousalPlaceholder;
            leftItem = setUp.leftItem;
            rightItem = setUp.rightItem;
            isOtherRegionClickable = setUp.isOtherRegionClickable;
            isImagesInCarousel = setUp.isImagesInCarousel;


            setUpBitCarouselBitmapsFromSetUp();

        } else if (carousel != null && carouselNotificationId != setUp.carouselNotificationId) {
            carousel = null;
            verifyAndSetUpVariables(setUp);
        }
    }

    /**
     * If exists it loads bitmaps from file directory and saves them.
     */
    private void setUpBitCarouselBitmapsFromSetUp() {
        if (smallIconPath != null) {
            smallIcon = ImageUtils.loadImageFromStorage(smallIconPath,   Constants.CAROUSAL_SMALL_ICON_FILE_NAME);
        }
        if (largeIconPath != null) {
            largeIcon = ImageUtils.loadImageFromStorage(largeIconPath,   Constants.CAROUSAL_LARGE_ICON_FILE_NAME);
        }
        if (placeHolderImagePath != null) {
            caraousalPlaceholder = ImageUtils.loadImageFromStorage(placeHolderImagePath,   Constants.CAROUSAL_PLACEHOLDER_ICON_FILE_NAME);
        }
    }

    private void onRightItemClicked() {
        sendItemClickedBroadcast(rightItem);
    }

    private void onLeftItemClicked() {
        sendItemClickedBroadcast(leftItem);

    }

    private void onOtherRegionClicked() {
        if (isOtherRegionClickable) {
            //We will send the broadcast and finish the carousel
            Intent i = new Intent();
            i.setAction(  Constants.CAROUSAL_ITEM_CLICKED_INTENT_FILTER);
            Bundle bundle = new Bundle();
            i.putExtras(bundle);
            context.getApplicationContext().sendBroadcast(i);
            try {
                clearCarouselIfExists();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Unable To send notification's pendingIntent");
            }
        }
    }

    private void sendItemClickedBroadcast(CarouselItem cItem) {
        Intent i = new Intent();
        i.setAction(  Constants.CAROUSAL_ITEM_CLICKED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CAROUSAL_ITEM_CLICKED_KEY, cItem);
        i.putExtras(bundle);

        context.getApplicationContext().sendBroadcast(i);


        try {
            clearCarouselIfExists();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Unable To send notification's pendingIntent");
        }
    }

    private void onLeftArrowClicked() {

        if (carouselItems != null && carouselItems.size() > currentStartIndex) {

            switch (currentStartIndex) {

                case 1:
                    currentStartIndex = carouselItems.size() - 1;
                    prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), carouselItems.get(0));
                    break;
                case 0:
                    currentStartIndex = carouselItems.size() - 2;
                    prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), carouselItems.get(currentStartIndex + 1));
                    break;
                default:
                    currentStartIndex -= 2;
                    prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), carouselItems.get(currentStartIndex + 1));
                    break;
            }
        }
    }

    private void onRightArrowClicked() {
        if (carouselItems != null && carouselItems.size() > currentStartIndex) {

            int difference = carouselItems.size() - currentStartIndex;
            switch (difference) {
                case 3:
                    currentStartIndex += 2;
                    prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), carouselItems.get(0));
                    break;
                case 2:
                    currentStartIndex = 0;
                    prepareVariablesForCarouselAndShow(carouselItems.get(0), carouselItems.get(1));
                    break;
                case 1:
                    currentStartIndex = 1;
                    prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), carouselItems.get(currentStartIndex + 1));
                    break;
                default:
                    currentStartIndex += 2;
                    prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex), carouselItems.get(currentStartIndex + 1));
                    break;
            }
        }
    }
}