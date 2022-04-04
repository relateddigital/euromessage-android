package euromsg.com.euromobileandroid.enums;

import java.io.Serializable;

public enum PushType implements Serializable {
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