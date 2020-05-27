package euromsg.com.euromobileandroid.enums;

public enum PushPermit {
    ACTIVE("A"), PASSIVE("N");

    public final String name;

    PushPermit(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}