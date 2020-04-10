package euromsg.com.euromobileandroid.enums;

public enum PushType {
    Text("Text"), Image("Image"), Carousel("Carousel"), Video("Video");

    private final String name;

    PushType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}