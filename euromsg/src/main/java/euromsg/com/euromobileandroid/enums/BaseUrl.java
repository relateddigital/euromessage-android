package euromsg.com.euromobileandroid.enums;


public enum BaseUrl {
    Retention("https://pushr.euromsg.com/retention/"), Subscription("https://pushs.euromsg.com/subscription/");

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