package euromsg.com.euromobileandroid.model;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import euromsg.com.euromobileandroid.enums.PushType;
import euromsg.com.euromobileandroid.utils.LogUtils;

public class Message implements Serializable {

    private String date;
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
    private String deliver;
    private String silent;
    private PushType pushType;
    private String collapseKey;
    private Map<String, String> params = new HashMap<>();
    private ArrayList<Element> elements;

    public Message(Context context, @NonNull Map<String, String> bundle) {
        try {
            for (String key : bundle.keySet()) {

                Object value = bundle.get(key);
                if (value != null) {
                    params.put(key, value.toString());
                }
            }
            date = bundle.get("date");
            mediaUrl = bundle.get("mediaUrl");
            pushId = bundle.get("pushId");
            url = bundle.get("url");
            altUrl = bundle.get("altUrl");
            from = bundle.get("from");
            message = bundle.get("message");
            title = bundle.get("title");
            sound = bundle.get("sound");
            emPushSp = bundle.get("emPushSp");
            deliver = bundle.get("deliver");
            silent = bundle.get("silent");
            campaignId = bundle.get("cId");
            if (bundle.get("pushType") != null) {
                pushType = PushType.valueOf(bundle.get("pushType"));
            } else {
                pushType = PushType.Text;
            }
            collapseKey = bundle.get("collapse_key");

            if (bundle.get("elements") != null) {
                convertJsonStrToElementsArray(context, bundle.get("elements"));
            }
        } catch (Exception e) {
            Log.e("Message", "Could not model the message!");
            e.printStackTrace();
        }
    }

    private void convertJsonStrToElementsArray(Context context, String elementJsonStr) {

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
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Converting JSON string to array list : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            e.printStackTrace();
        }
    }

    public Message(@NonNull Bundle bundle) {
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            params.put(key, value.toString());
        }
        date = bundle.getString("date");
        mediaUrl = bundle.getString("mediaUrl");
        pushId = bundle.getString("pushId");
        url = bundle.getString("url");
        altUrl = bundle.getString("altUrl");
        from = bundle.getString("from");
        message = bundle.getString("message");
        title = bundle.getString("title");
        sound = bundle.getString("sound");
        emPushSp = bundle.getString("emPushSp");
        deliver = bundle.getString("deliver");
        silent = bundle.getString("silent");
        campaignId = bundle.getString("cId");
        if (bundle.getString("pushType") != null) {
            pushType = PushType.valueOf(bundle.getString("pushType"));
        }
        collapseKey = bundle.getString("collapse_key");
        elements = bundle.getParcelable("elements");
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAltUrl() {
        return altUrl;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getDate() {
        return date;
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

    public String getDeliver() {
        return deliver;
    }

    public String getSilent() {
        return silent;
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