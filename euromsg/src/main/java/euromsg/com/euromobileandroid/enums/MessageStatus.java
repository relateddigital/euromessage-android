package euromsg.com.euromobileandroid.enums;

public enum MessageStatus {
    Read("O"), Received("D"), Silent("S");

    private final String name;

    private MessageStatus(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName != null) && name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}