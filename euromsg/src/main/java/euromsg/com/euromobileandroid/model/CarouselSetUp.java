package euromsg.com.euromobileandroid.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;


public class CarouselSetUp implements Parcelable {

    public ArrayList<CarouselItem> carouselItems;

    public String contentTitle;
    public String contentText;
    public String bigContentTitle;
    public String bigContentText;
    public int carouselNotificationId; //Random id 9873715 for notification. Will cancel any notification that have existing same id.
    public  int currentStartIndex; //Variable that keeps track of where the startIndex is
    public String smallIcon;
    private int smallIconResourceId;
    public String largeIcon;
    public String caraousalPlaceholder;
    public CarouselItem leftItem;
    public CarouselItem rightItem;
    public boolean isOtherRegionClickable;
    public boolean isImagesInCarousel;

    public CarouselSetUp(ArrayList<CarouselItem> carouselItems, String contentTitle, String contentText,
                  String bigContentTitle, String bigContentText, int carouselNotificationId,
                  int currentStartIndex, String smallIcon, int smallIconResourceId,
                  String largeIcon, String caraousalPlaceholder, CarouselItem leftItem,
                  CarouselItem rightItem, boolean isOtherRegionClickable, boolean isImagesInCarousel) {
        this.carouselItems = carouselItems;
        this.contentTitle = contentTitle;
        this.contentText = contentText;
        this.bigContentTitle = bigContentTitle;
        this.bigContentText = bigContentText;
        this.carouselNotificationId = carouselNotificationId;
        this.currentStartIndex = currentStartIndex;
        this.smallIcon = smallIcon;
        this.smallIconResourceId = -1;
        this.smallIconResourceId = smallIconResourceId;
        this.largeIcon = largeIcon;
        this.caraousalPlaceholder = caraousalPlaceholder;
        this.leftItem = leftItem;
        this.rightItem = rightItem;
        this.isOtherRegionClickable = isOtherRegionClickable;
        this.isImagesInCarousel = isImagesInCarousel;
    }

    private CarouselSetUp(Parcel in) {
        if (in.readByte() == 0x01) {
            carouselItems = new ArrayList<>();
            in.readList(carouselItems, CarouselItem.class.getClassLoader());
        } else {
            carouselItems = null;
        }
        contentTitle = in.readString();
        contentText = in.readString();
        bigContentTitle = in.readString();
        bigContentText = in.readString();
        carouselNotificationId = in.readInt();
        currentStartIndex = in.readInt();
        smallIcon = in.readString();
        smallIconResourceId = -1;
        smallIconResourceId = in.readInt();
        largeIcon = in.readString();
        caraousalPlaceholder = in.readString();
        leftItem = (CarouselItem) in.readValue(CarouselItem.class.getClassLoader());
        rightItem = (CarouselItem) in.readValue(CarouselItem.class.getClassLoader());
        isOtherRegionClickable = in.readByte() != 0x00;
        isImagesInCarousel = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (carouselItems == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(carouselItems);
        }
        dest.writeString(contentTitle);
        dest.writeString(contentText);
        dest.writeString(bigContentTitle);
        dest.writeString(bigContentText);
        dest.writeInt(carouselNotificationId);
        dest.writeInt(currentStartIndex);
        dest.writeString(smallIcon);
        dest.writeInt(smallIconResourceId);
        dest.writeString(largeIcon);
        dest.writeString(caraousalPlaceholder);
        dest.writeValue(leftItem);
        dest.writeValue(rightItem);
        dest.writeByte((byte) (isOtherRegionClickable ? 0x01 : 0x00));
        dest.writeByte((byte) (isImagesInCarousel ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Creator<CarouselSetUp> CREATOR = new Creator<CarouselSetUp>() {
        @Override
        public CarouselSetUp createFromParcel(Parcel in) {
            return new CarouselSetUp(in);
        }

        @Override
        public CarouselSetUp[] newArray(int size) {
            return new CarouselSetUp[size];
        }
    };
}