package euromsg.com.euromobileandroid.carousalnotification;

import android.os.Parcel;
import android.os.Parcelable;


public class CarousalItem implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String photoUrl;
    private String imageFileLocation;
    private String imageFileName;
    private String type;

    public CarousalItem( String photoUrl) {
        this(null, null, null, photoUrl);
    }

    CarousalItem() {
        this(null, null, null, null);
    }

    public CarousalItem(String id, String title, String description, String photoUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    String getImageFileLocation() {
        return imageFileLocation;
    }

    public void setImageFileLocation(String imageFileLocation) {
        this.imageFileLocation = imageFileLocation;
    }

    String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private CarousalItem(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        photoUrl = in.readString();
        imageFileLocation = in.readString();
        imageFileName = in.readString();
        type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(photoUrl);
        dest.writeString(imageFileLocation);
        dest.writeString(imageFileName);
        dest.writeString(type);
    }

    @SuppressWarnings("unused")
    public static final Creator<CarousalItem> CREATOR = new Creator<CarousalItem>() {
        @Override
        public CarousalItem createFromParcel(Parcel in) {
            return new CarousalItem(in);
        }

        @Override
        public CarousalItem[] newArray(int size) {
            return new CarousalItem[size];
        }
    };
}