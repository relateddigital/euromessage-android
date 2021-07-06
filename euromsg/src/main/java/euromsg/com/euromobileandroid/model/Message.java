package euromsg.com.euromobileandroid.model;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import euromsg.com.euromobileandroid.enums.PushType;

public class Message implements Serializable {

    private String mediaUrl;
    private String altUrl;
    private String pushId;
    private String campaignId;
    private String url;
    private String from;
    private String message;
    private String title;
    private String sound;
    private String emPushSp;
    private PushType pushType;
    private String collapseKey;
    private Map<String, String> params = new HashMap<>();
    private ArrayList<Element> elements;

    public Message(@NonNull Map<String, String> bundle) {

        for (String key : bundle.keySet()) {

            Object value = bundle.get(key);
            if (value != null) {
                params.put(key, value.toString());
            }
        }
        mediaUrl = bundle.get("mediaUrl");
        pushId = bundle.get("pushId");
        url = bundle.get("url");
        altUrl = bundle.get("altUrl");
        from = bundle.get("from");
        message = bundle.get("message");
        title = bundle.get("title");
        sound = bundle.get("sound");
        emPushSp = bundle.get("emPushSp");
        campaignId = bundle.get("cId");
        if (bundle.get("pushType") != null) {
            pushType = PushType.valueOf(bundle.get("pushType"));
        }else {
            pushType = PushType.Text;
        }
        collapseKey = bundle.get("collapse_key");

        if (bundle.get("elements") != null) {
            convertJsonStrToElementsArray(bundle.get("elements"));
        }
    }

    private void convertJsonStrToElementsArray(String elementJsonStr) {

        JSONArray jsonArr;

        try {
            jsonArr = new JSONArray(elementJsonStr);
            elements = new ArrayList<>();
            for (int i = 0; i < jsonArr.length(); i++) {

                JSONObject jsonObj = jsonArr.getJSONObject(i);

                Element element = new Element();
                element.setId(jsonObj.getString("id"));
                element.setTitle(jsonObj.getString("title"));
                element.setContent(jsonObj.getString("content"));
                element.setPicture(jsonObj.getString("picture"));
                element.setUrl(jsonObj.getString("url"));

                elements.add(element);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Message(@NonNull Bundle bundle) {
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            params.put(key, value.toString());
        }
        mediaUrl = bundle.getString("mediaUrl");
        pushId = bundle.getString("pushId");
        url = bundle.getString("url");
        altUrl = bundle.getString("altUrl");
        from = bundle.getString("from");
        message = bundle.getString("message");
        title = bundle.getString("title");
        sound = bundle.getString("sound");
        emPushSp = bundle.getString("emPushSp");
        campaignId = bundle.getString("cId");
        if (bundle.getString("pushType") != null) {
            pushType = PushType.valueOf(bundle.getString("pushType"));
        }
        collapseKey = bundle.getString("collapse_key");
        elements = bundle.getParcelable("elements");
    }

    public String getAltUrl() {
        return altUrl;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getFrom() {
        return from;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public PushType getPushType() {
        return pushType;
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    public String getEmPushSp() {
        return emPushSp;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getPushId() {
        return pushId;
    }

    public String getSound() {
        return sound;
    }

    public ArrayList<Element> getElements() {
        return elements;
    }
}