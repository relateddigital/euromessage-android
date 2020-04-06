package euromsg.com.euromobileandroid.enums;


public enum BaseUrl {
    Retention("https://test.euromsg.com:4243/"), Subscription("https://test.euromsg.com:4242/");

    private final String name;

    private BaseUrl(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName != null) && name.equals(otherName);
    }

    public String toString() {
        return name;
    }
}