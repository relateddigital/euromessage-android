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
import java.util.List;
import java.util.Map;

import euromsg.com.euromobileandroid.enums.PushType;
import euromsg.com.euromobileandroid.utils.LogUtils;

public class Message implements Serializable {

    private String date;
    private String openDate;
    private String status;
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
    private Integer notificationId;
    private String collapseKey;
    private Map<String, String> params; // Değişiklik: Başlangıçta null bırakıldı.
    private ArrayList<Element> elements;
    private ArrayList<Actions> actions;
    private String loginID;
    private String pushCategory;
    private String keyID;
    private String email;

    // extraData alanı kaldırıldı.

    public Message(Context context, @NonNull Map<String, String> bundle) {
        try {
            // Değişiklik: Gelen tüm bundle verisini doğrudan params'a kopyala.
            this.params = new HashMap<>(bundle);

            // Değişiklik: Değerleri artık bundle yerine params'tan al.
            // Bu, kodun tutarlılığını artırır.
            date = params.get("date");
            openDate = params.get("openDate");
            status = params.get("status");
            mediaUrl = params.get("mediaUrl");
            pushId = params.get("pushId");
            url = params.get("url");
            altUrl = params.get("altUrl");
            from = params.get("from");
            message = params.get("message");
            title = params.get("title");
            sound = params.get("sound");
            if(sound == null) {
                sound = "";
            }
            emPushSp = params.get("emPushSp");
            deliver = params.get("deliver");
            silent = params.get("silent");
            campaignId = params.get("cId");
            if (params.get("pushType") != null) {
                pushType = PushType.valueOf(params.get("pushType"));
            } else {
                pushType = PushType.Text;
            }
            collapseKey = params.get("collapse_key");

            if (params.get("elements") != null) {
                convertJsonStrToElementsArray(context, params.get("elements"));
            }
            if (params.get("actions") != null) {
                convertJsonStrToActionsArray(context, params.get("actions"));
            }
            pushCategory = params.get("pushCategory");
            keyID = params.get("keyID");
            email = params.get("email");
        } catch (Exception e) {
            Log.e("Message", "Could not model the message!");
            e.printStackTrace();
        }
    }

    // isKnownKey metodu artık gerekli olmadığı için kaldırıldı.

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

    private void convertJsonStrToActionsArray(Context context, String actionsJsonStr) {
        JSONArray jsonArr;
        try {
            jsonArr = new JSONArray(actionsJsonStr);
            actions = new ArrayList<>();
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                Actions action = new Actions();
                action.setAction(jsonObj.getString("action"));
                action.setTitle(jsonObj.getString("title"));
                action.setIcon(jsonObj.getString("icon"));
                action.setUrl(jsonObj.getString("url"));
                actions.add(action);
            }
        } catch (JSONException e) {
            StackTraceElement action = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Converting JSON string to array list : " + e.getMessage(),
                    action.getClassName() + "/" + action.getMethodName() + "/" + action.getLineNumber()
            );
            e.printStackTrace();
        }
    }

    public Message(@NonNull Bundle bundle) {
        // Bu constructor'ı da güncelleyelim, tutarlı olsun.
        this.params = new HashMap<>();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value != null) {
                params.put(key, value.toString());
            }
        }
        date = params.get("date");
        openDate = params.get("openDate");
        status = params.get("status");
        mediaUrl = params.get("mediaUrl");
        pushId = params.get("pushId");
        url = params.get("url");
        altUrl = params.get("altUrl");
        from = params.get("from");
        message = params.get("message");
        title = params.get("title");
        sound = params.get("sound");
        if(sound == null) {
            sound = "";
        }
        emPushSp = params.get("emPushSp");
        deliver = params.get("deliver");
        silent = params.get("silent");
        campaignId = params.get("cId");
        if (params.get("pushType") != null) {
            pushType = PushType.valueOf(params.get("pushType"));
        }
        collapseKey = params.get("collapse_key");
        elements = bundle.getParcelable("elements"); // Bundle'dan gelen parcelable özel durum
        pushCategory = params.get("pushCategory");
        keyID = params.get("keyID");
        email = params.get("email");
    }

    // ----- GETTER ve SETTER Metotları (Değişiklik yok) -----

    public void setDate(String date) { this.date = date; }
    public void setOpenDate(String openDate) { this.openDate = openDate; }
    public void setKeyID(String keyID) { this.keyID = keyID; }
    public void setEmail(String email) { this.email = email; }
    public void setStatus(String status) { this.status = status; }
    public Integer setNotifiactionId() { return notificationId; }
    public Integer getNotificationId() { return notificationId; }
    public String getAltUrl() { return altUrl; }
    public String getCampaignId() { return campaignId; }
    public String getDate() { return date; }
    public String getOpenDate() { return openDate; }
    public String getStatus() { return status; }
    public String getMediaUrl() { return mediaUrl; }
    public String getUrl() { return url; }
    public String getFrom() { return from; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public PushType getPushType() { return pushType; }
    public String getCollapseKey() { return collapseKey; }
    public void setLoginID(String loginID) { this.loginID = loginID; }
    public String getLoginID() { return loginID; }
    public String getEmPushSp() { return emPushSp; }
    public String getDeliver() { return deliver; }
    public String getSilent() { return silent; }
    public Map<String, String> getParams() { return params; }
    public String getPushId() { return pushId; }
    public String getSound() { return sound; }
    public ArrayList<Element> getElements() { return elements; }
    public ArrayList<Actions> getActions() { return actions; }
    public String getPushCategory() { return pushCategory; }
    public String getKeyID() { return keyID; }
    public String getEmail() { return email; }

    // getExtraData metodu artık gerekli olmadığı için kaldırıldı.
}