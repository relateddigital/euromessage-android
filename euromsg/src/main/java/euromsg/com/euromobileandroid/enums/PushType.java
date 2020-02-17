package euromsg.com.euromobileandroid.enums;

/**
 * Created by ozanuysal on 25/01/15.
 */
public enum PushType {
    Text("Text"), Image("Image"), Video("Video"), Background("Background"), Survey(
            "Survey");

    private final String name;

    private PushType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName != null) && name.equals(otherName);
    }

    public String toString() {
        return name;
    }

}
