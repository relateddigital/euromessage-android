package euromsg.com.euromobileandroid.enums;

public enum EmailPermit {
    ACTIVE("Y"),  PASSIVE("N");

    public final String name;

    EmailPermit(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}