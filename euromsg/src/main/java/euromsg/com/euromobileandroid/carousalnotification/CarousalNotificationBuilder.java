package euromsg.com.euromobileandroid.carousalnotification;

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

import java.util.ArrayList;

import euromsg.com.euromobileandroid.R;
import euromsg.com.euromobileandroid.connection.ImageDownloaderManager;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.Utils;

public class CarousalNotificationBuilder {

    private static CarousalNotificationBuilder carousalNotificationBuilder;
    private Context context;
    private ArrayList<CarousalItem> carousalItems;
    private String contentTitle, contentText; //title and text while it is small
    private String bigContentTitle, bigContentText; //title and text when it becomes large
    private String leftItemTitle, leftItemDescription;
    private String rightItemTitle, rightItemDescription;

    private static final String TAG = "Carousal";
    private NotificationCompat.Builder mBuilder;
    private int carousalNotificationId = 9873715; //Random id for notification. Will cancel any
    // notification that have existing same id.

    private static int currentStartIndex = 0; //Variable that keeps track of where the startIndex is
    private static int notificationPriority = NotificationCompat.PRIORITY_DEFAULT;
    private Notification foregroundNote;

    private static Bitmap appIcon;
    private static Bitmap smallIcon;
    private static int smallIconResourceId = -1; //check before setting it that it does exists
    private static Bitmap largeIcon;
    private static Bitmap caraousalPlaceholder;

    private CarousalItem leftItem, rightItem;
    private Bitmap leftItemBitmap, rightItemBitmap;

    private CarousalSetUp carousalSetUp;
    private String smallIconPath, largeIconPath, placeHolderImagePath; //Stores path of these images if set by user

    private boolean isOtherRegionClickable = false;
    private boolean isImagesInCarousal = true;

    private String channelId = "euroChannel";

    private static final String CAROUSAL_ITEM_CLICKED_KEY = "CarousalNotificationItemClickedKey";

    private CarousalNotificationBuilder(Context context) {
        this.context = context;
        mBuilder = new NotificationCompat.Builder(context, channelId);
    }

    public static CarousalNotificationBuilder with(Context context) {
        if (carousalNotificationBuilder == null) {
            synchronized (CarousalNotificationBuilder.class) {
                if (carousalNotificationBuilder == null) {
                    carousalNotificationBuilder = new CarousalNotificationBuilder(context);
                    try {
                        appIcon = CarousalUtilities.carousalDrawableToBitmap(context.getPackageManager().getApplicationIcon(context.getPackageName()));
                    } catch (PackageManager.NameNotFoundException e) {
                        appIcon = null;
                        Log.e(TAG, "Unable to retrieve app Icon");
                    }

                }
            }
        }
        return carousalNotificationBuilder;
    }

    public CarousalNotificationBuilder beginTransaction() {
        clearCarousalIfExists();
        return this;
    }

    public void addCarousalItem(CarousalItem carousalItem) {
        if (carousalItem != null) {
            if (carousalItems == null) {
                carousalItems = new ArrayList<>();
            }
            carousalItems.add(carousalItem);
        } else {
            Log.e(TAG, "Null carousal can't be added!");
        }
    }

    public CarousalNotificationBuilder setContentTitle(String title) {
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

    public CarousalNotificationBuilder setNotificationPriority(int priority) {
        if (priority >= NotificationCompat.PRIORITY_MIN && priority <= NotificationCompat.PRIORITY_MAX) {
            notificationPriority = priority;
        } else {
            Log.i(TAG, "Invalid priority");
        }
        return this;
    }

    public CarousalNotificationBuilder setSmallIconResource(int resourceId) {
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

    public CarousalNotificationBuilder setLargeIcon(int resourceId) {
        try {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            Log.e(TAG, "Unable to decode resource");
        }
        return this;
    }

    public CarousalNotificationBuilder setLargeIcon(Bitmap large) {
        if (large != null) {
            largeIcon = large;
        } else {
            largeIcon = null;
            Log.i(TAG, "Null parameter");
        }
        return this;
    }

    public void setCarousalPlaceHolder(int resourceId) {
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

    public void buildCarousal() {

        boolean isImagesInCarous = false;
        int numberofImages = 0;
        if (carousalItems != null && carousalItems.size() > 0) {
            for (CarousalItem item : carousalItems) {
                if (!TextUtils.isEmpty(item.getPhotoUrl())) {
                    isImagesInCarous = true;
                    numberofImages++;
                }
            }
            if (isImagesInCarous) {
                ImageDownloaderManager imageDownloaderManager = new ImageDownloaderManager(context, carousalItems
                        , numberofImages, new ImageDownloaderManager.OnDownloadsCompletedListener() {
                    @Override
                    public void onComplete() {
                        initiateCarousalTransaction();
                    }
                });
                imageDownloaderManager.startAllDownloads();
            } else {
                this.isImagesInCarousal = false;
                initiateCarousalTransaction();
            }
        }
    }

    private void initiateCarousalTransaction() {
        currentStartIndex = 0;
        if (carousalItems != null && carousalItems.size() > 0) {
            if (carousalItems.size() == 1) {
                prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), null);
            } else {
                prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), carousalItems.get(currentStartIndex + 1));
            }
        }
    }

    private void prepareVariablesForCarousalAndShow(CarousalItem leftItem, CarousalItem rightItem) {
        if (this.leftItem == null) {
            this.leftItem = new CarousalItem();
        }
        if (this.rightItem == null) {
            this.rightItem = new CarousalItem();
        }
        if (leftItem != null) {
            this.leftItem = leftItem;
            leftItemTitle = leftItem.getTitle();
            leftItemDescription = leftItem.getDescription();
            leftItemBitmap = getCarousalBitmap(leftItem);

        }
        if (rightItem != null) {
            this.rightItem = rightItem;
            rightItemTitle = rightItem.getTitle();
            rightItemDescription = rightItem.getDescription();
            rightItemBitmap = getCarousalBitmap(rightItem);

        }
        showCarousal();
    }

    private void showCarousal() {

        if (carousalItems != null && carousalItems.size() > 0) {

            if (carousalSetUp == null || carousalSetUp.carousalNotificationId != carousalNotificationId) {
                //First save this set up into a carousal setup item
                carousalSetUp = saveCarousalSetUp();
            } else {
                carousalSetUp.currentStartIndex = currentStartIndex;
                carousalSetUp.leftItem = leftItem;
                carousalSetUp.rightItem = rightItem;
            }

            setUpCarousalIcons();
            setUpCarousalTitles();

            RemoteViews bigView = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.carousal_notification_item);

            setUpCarousalVisibilities(bigView);
            setUpCarousalItems(bigView);
            setPendingIntents(bigView);

            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            PushNotificationManager pushNotificationManager = new PushNotificationManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotifyManager != null) {
                pushNotificationManager.createNotificationChannel(mNotifyManager, channelId);
            }

            mBuilder = pushNotificationManager.createNotificationBuilder(context, contentTitle, contentText);

            if (isOtherRegionClickable) {
                setOtherRegionClickable();
            }

            foregroundNote = mBuilder.build();
            foregroundNote.bigContentView = bigView;

            if (mNotifyManager != null) {
                mNotifyManager.notify(carousalNotificationId, foregroundNote);
            }
        } else {
            Log.e(TAG, "Empty item array or of length less than 2");
        }

    }

    private void setOtherRegionClickable() {

        Intent carousalIntent = new Intent(CarousalConstants.CAROUSAL_EVENT_FIRED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putInt(CarousalConstants.EVENT_CAROUSAL_ITEM_CLICKED_KEY, CarousalConstants.EVENT_OTHER_REGION_CLICKED);
        bundle.putParcelable(CarousalConstants.CAROUSAL_SET_UP_KEY, carousalSetUp);
        carousalIntent.putExtras(bundle);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, CarousalConstants.EVENT_OTHER_REGION_CLICKED, carousalIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);
    }

    private Bitmap getCarousalBitmap(CarousalItem item) {
        Bitmap bitmap = null;
        if (item != null) {
            if (!TextUtils.isEmpty(item.getImageFileName()) && !TextUtils.isEmpty(item.getImageFileLocation())) {
                bitmap = CarousalUtilities.carousalLoadImageFromStorage(item.getImageFileLocation(), item.getImageFileName());
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

    private void setUpCarousalVisibilities(RemoteViews bigView) {

        if (carousalItems.size() < 3) {
            bigView.setViewVisibility(R.id.ivArrowLeft, View.GONE);
            bigView.setViewVisibility(R.id.ivArrowRight, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivArrowLeft, View.VISIBLE);
            bigView.setViewVisibility(R.id.ivArrowRight, View.VISIBLE);
        }
        if (carousalItems.size() < 2) {
            bigView.setViewVisibility(R.id.llRightItemLayout, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.llRightItemLayout, View.VISIBLE);
        }
        if (TextUtils.isEmpty(bigContentText)) {
            bigView.setViewVisibility(R.id.tvCarousalContent, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvCarousalContent, View.VISIBLE);
        }
        if (TextUtils.isEmpty(bigContentTitle)) {
            bigView.setViewVisibility(R.id.tvCarousalTitle, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvCarousalTitle, View.VISIBLE);
        }
        if (TextUtils.isEmpty(leftItemTitle)) {
            bigView.setViewVisibility(R.id.tvLeftTitleText, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvLeftTitleText, View.VISIBLE);
        }
        if (TextUtils.isEmpty(leftItemDescription)) {
            bigView.setViewVisibility(R.id.tvLeftDescriptionText, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvLeftDescriptionText, View.VISIBLE);
        }
        if (TextUtils.isEmpty(rightItemTitle)) {
            bigView.setViewVisibility(R.id.tvRightTitleText, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvRightTitleText, View.VISIBLE);
        }
        if (TextUtils.isEmpty(rightItemDescription)) {
            bigView.setViewVisibility(R.id.tvRightDescriptionText, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvRightDescriptionText, View.VISIBLE);
        }
        if (!isImagesInCarousal) {
            bigView.setViewVisibility(R.id.ivImageLeft, View.GONE);
            bigView.setViewVisibility(R.id.ivImageRight, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivImageLeft, View.VISIBLE);
            bigView.setViewVisibility(R.id.ivImageRight, View.VISIBLE);
        }

    }

    private void setUpCarousalTitles() {
        if (TextUtils.isEmpty(contentTitle)) {
            setContentTitle(CarousalUtilities.carousalGetApplicationName(context));
        }

        if (bigContentTitle == null)
            bigContentTitle = "";
        if (bigContentText == null)
            bigContentText = "";
    }

    private void setUpCarousalIcons() {
        if (appIcon != null) {
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        } else {
            appIcon = BitmapFactory.decodeResource(context.getResources(), Utils.getAppIcon(context));
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = CarousalUtilities.carousalGetAppIconResourceId(context);
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = R.drawable.ic_carousal_icon;
        }
    }

    private void setUpCarousalItems(RemoteViews bigView) {
        if (leftItemBitmap != null) {
            bigView.setImageViewBitmap(R.id.ivImageLeft, leftItemBitmap);
        }
        if (rightItemBitmap != null) {
            bigView.setImageViewBitmap(R.id.ivImageRight, rightItemBitmap);
        }
        bigView.setImageViewBitmap(R.id.ivCarousalAppIcon, largeIcon);
        bigView.setTextViewText(R.id.tvCarousalTitle, bigContentTitle);
        bigView.setTextViewText(R.id.tvCarousalContent, bigContentText);
        bigView.setTextViewText(R.id.tvRightTitleText, rightItemTitle);
        bigView.setTextViewText(R.id.tvRightDescriptionText, rightItemDescription);
        bigView.setTextViewText(R.id.tvLeftTitleText, leftItemTitle);
        bigView.setTextViewText(R.id.tvLeftDescriptionText, leftItemDescription);
    }

    private void setPendingIntents(RemoteViews bigView) {
        //right arrow
        PendingIntent rightArrowPendingIntent = getPendingIntent(CarousalConstants.EVENT_RIGHT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ivArrowRight, rightArrowPendingIntent);
        //left arrow
        PendingIntent leftArrowPendingIntent = getPendingIntent(CarousalConstants.EVENT_LEFT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ivArrowLeft, leftArrowPendingIntent);
        //right item
        PendingIntent rightItemPendingIntent = getPendingIntent(CarousalConstants.EVENT_RIGHT_ITEM_CLICKED);
        bigView.setOnClickPendingIntent(R.id.llRightItemLayout, rightItemPendingIntent);
        //left item
        PendingIntent leftItemPendingIntent = getPendingIntent(CarousalConstants.EVENT_LEFT_ITEM_CLICKED);
        bigView.setOnClickPendingIntent(R.id.llLeftItemLayout, leftItemPendingIntent);
    }

    private PendingIntent getPendingIntent(int eventClicked) {

        Intent carousalIntent = new Intent(context, CarousalEventReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt(CarousalConstants.EVENT_CAROUSAL_ITEM_CLICKED_KEY, eventClicked);
        bundle.putParcelable(CarousalConstants.CAROUSAL_SET_UP_KEY, carousalSetUp);
        carousalIntent.putExtras(bundle);
        return PendingIntent.getBroadcast(context, eventClicked, carousalIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private CarousalSetUp saveCarousalSetUp() {
        setUpfilePathOfImages();
        return new CarousalSetUp(carousalItems, contentTitle, contentText,
                bigContentTitle, bigContentText, carousalNotificationId,
                currentStartIndex, smallIconPath, smallIconResourceId, largeIconPath,
                placeHolderImagePath, leftItem, rightItem, isOtherRegionClickable, isImagesInCarousal);
    }

    private void setUpfilePathOfImages() {
        if (smallIcon != null) {
            smallIconPath = CarousalUtilities.carousalSaveBitmapToInternalStorage(context, smallIcon,
                    CarousalConstants.CAROUSAL_SMALL_ICON_FILE_NAME);
        }
        if (largeIcon != null) {
            largeIconPath = CarousalUtilities.carousalSaveBitmapToInternalStorage(context, largeIcon,
                    CarousalConstants.CAROUSAL_LARGE_ICON_FILE_NAME);
        }
        if (caraousalPlaceholder != null) {
            placeHolderImagePath = CarousalUtilities.carousalSaveBitmapToInternalStorage(context, caraousalPlaceholder,
                    CarousalConstants.CAROUSAL_PLACEHOLDER_ICON_FILE_NAME);
        }
    }

    private void clearCarousalIfExists() {
        if (carousalItems != null) {
            /*for (CarousalItem cr : carousalItems) {
                if (cr.getImageFileName() != null)
                if(context.deleteFile(cr.getImageFileName()))
                    Log.i(TAG, "Image deleted.");
            }*/
            carousalItems.clear();

            smallIconResourceId = -1;
            isOtherRegionClickable = false;
            isImagesInCarousal = true;
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
                mNotifyManager.cancel(carousalNotificationId);
            }
        }
        //ToDo :  delete all cache files
    }

    void handleClickEvent(int clickEvent, CarousalSetUp setUp) {

          verifyAndSetUpVariables(setUp);

        switch (clickEvent) {
            case CarousalConstants.EVENT_LEFT_ARROW_CLICKED:
                onLeftArrowClicked();
                break;
            case CarousalConstants.EVENT_RIGHT_ARROW_CLICKED:
                onRightArrowClicked();
                break;
            case CarousalConstants.EVENT_LEFT_ITEM_CLICKED:
                onLeftItemClicked();
                break;
            case CarousalConstants.EVENT_RIGHT_ITEM_CLICKED:
                onRightItemClicked();
                break;
            case CarousalConstants.EVENT_OTHER_REGION_CLICKED:
                onOtherRegionClicked ();
                break;
            default:
                break;
        }
    }

    private void verifyAndSetUpVariables(CarousalSetUp setUp) {

        if (carousalSetUp == null) {

            carousalItems = setUp.carousalItems;
            contentTitle = setUp.contentTitle;
            contentText = setUp.contentText;
            bigContentTitle = setUp.bigContentTitle;
            bigContentText = setUp.bigContentText;
            carousalNotificationId = setUp.carousalNotificationId;
            currentStartIndex = setUp.currentStartIndex;
            notificationPriority = setUp.notificationPriority;
            smallIconPath = setUp.smallIcon;
            largeIconPath = setUp.largeIcon;
            placeHolderImagePath = setUp.caraousalPlaceholder;
            leftItem = setUp.leftItem;
            rightItem = setUp.rightItem;
            isOtherRegionClickable = setUp.isOtherRegionClickable;
            isImagesInCarousal = setUp.isImagesInCarousal;


            setUpBitCarousalBitmapsFromSetUp();

        } else if (carousalSetUp != null && carousalNotificationId != setUp.carousalNotificationId) {
            carousalSetUp = null;
            verifyAndSetUpVariables(setUp);
        }
    }

    /**
     * If exists it loads bitmaps from file directory and saves them.
     */
    private void setUpBitCarousalBitmapsFromSetUp() {
        if (smallIconPath != null) {
            smallIcon = CarousalUtilities.carousalLoadImageFromStorage(smallIconPath, CarousalConstants.CAROUSAL_SMALL_ICON_FILE_NAME);
        }
        if (largeIconPath != null) {
            largeIcon = CarousalUtilities.carousalLoadImageFromStorage(largeIconPath, CarousalConstants.CAROUSAL_LARGE_ICON_FILE_NAME);
        }
        if (placeHolderImagePath != null) {
            caraousalPlaceholder = CarousalUtilities.carousalLoadImageFromStorage(placeHolderImagePath, CarousalConstants.CAROUSAL_PLACEHOLDER_ICON_FILE_NAME);
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
            //We will send the broadcast and finish the carousal
            Intent i = new Intent();
            i.setAction(CarousalConstants.CAROUSAL_ITEM_CLICKED_INTENT_FILTER);
            Bundle bundle = new Bundle();
            i.putExtras(bundle);
            context.getApplicationContext().sendBroadcast(i);
            try {
                clearCarousalIfExists();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Unable To send notification's pendingIntent");
            }
        }

    }

    private void sendItemClickedBroadcast(CarousalItem cItem) {
        Intent i = new Intent();
        i.setAction(CarousalConstants.CAROUSAL_ITEM_CLICKED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CAROUSAL_ITEM_CLICKED_KEY, cItem);
        i.putExtras(bundle);

        context.getApplicationContext().sendBroadcast(i);


        try {
            clearCarousalIfExists();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Unable To send notification's pendingIntent");
        }
    }

    private void onLeftArrowClicked() {

        if (carousalItems != null && carousalItems.size() > currentStartIndex) {

            switch (currentStartIndex) {

                case 1:
                    currentStartIndex = carousalItems.size() - 1;
                    prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), carousalItems.get(0));
                    break;
                case 0:
                    currentStartIndex = carousalItems.size() - 2;
                    prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), carousalItems.get(currentStartIndex + 1));
                    break;
                default:
                    currentStartIndex -= 2;
                    prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), carousalItems.get(currentStartIndex + 1));
                    break;
            }
        }
    }

    private void onRightArrowClicked() {
        if (carousalItems != null && carousalItems.size() > currentStartIndex) {

            int difference = carousalItems.size() - currentStartIndex;
            switch (difference) {
                case 3:
                    currentStartIndex += 2;
                    prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), carousalItems.get(0));
                    break;
                case 2:
                    currentStartIndex = 0;
                    prepareVariablesForCarousalAndShow(carousalItems.get(0), carousalItems.get(1));
                    break;
                case 1:
                    currentStartIndex = 1;
                    prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), carousalItems.get(currentStartIndex + 1));
                    break;
                default:
                    currentStartIndex += 2;
                    prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex), carousalItems.get(currentStartIndex + 1));
                    break;
            }
        }
    }
}