package euromsg.com.euromobileandroid.enums;

public enum GsmPermit {
    ACTIVE("Y"), PASSIVE("X");

    public final String name;

    GsmPermit(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}