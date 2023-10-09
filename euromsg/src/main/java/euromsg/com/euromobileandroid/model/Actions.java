package euromsg.com.euromobileandroid.model;

import java.io.Serializable;

public class Actions implements Serializable {
    private String Title = null;
    private String Action = null;
    private String Icon = null;
    private String Url = null;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }

    public String getIcon() {
        return Icon;
    }

    public void setIcon(String icon) {
        Icon = icon;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }
}
