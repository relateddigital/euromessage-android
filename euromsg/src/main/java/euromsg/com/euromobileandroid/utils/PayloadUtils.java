package euromsg.com.euromobileandroid.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.model.Message;

public final class PayloadUtils {
    private static final String LOG_TAG = "PayloadUtils";
    private static final long DATE_THRESHOLD = 30;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void addPushMessage(Context context, Message message) {
        String payloads = SharedPreference.getString(context, Constants.PAYLOAD_SP_KEY);
        if(!payloads.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payloads);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.PAYLOAD_SP_ARRAY_KEY);
                if(isPushIdAvailable(jsonArray, message)) {
                    return;
                }
                jsonArray = addNewOne(jsonArray, message);
                if(jsonArray == null) {
                    return;
                }
                jsonArray = removeOldOnes(jsonArray);
                JSONObject finalObject = new JSONObject();
                finalObject.put(Constants.PAYLOAD_SP_ARRAY_KEY, jsonArray);
                SharedPreference.saveString(context, Constants.PAYLOAD_SP_KEY, finalObject.toString());
            } catch (Exception e) {
                Log.e(LOG_TAG, "Something went wrong when adding the push message to shared preferences!");
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
            createAndSaveNewOne(context, message);
        }
    }

    private static boolean isPushIdAvailable(JSONArray jsonArray, Message message) {
        boolean res = false;
        for(int i = 0 ; i < jsonArray.length() ; i++) {
            try {
                if (jsonArray.getJSONObject(i).getString("pushId").equals(message.getPushId())){
                    res = true;
                    break;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        return res;
    }

    private static JSONArray addNewOne(JSONArray jsonArray, Message message){
        try {
            message.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            jsonArray.put(new JSONObject(new Gson().toJson(message)));
            return jsonArray;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not save the push message!");
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static JSONArray removeOldOnes(JSONArray jsonArray) {
        for(int i = 0 ; i < jsonArray.length() ; i++) {
            try {
                if (isOld(jsonArray.getJSONObject(i).getString("date"))){
                    jsonArray.remove(i);
                    i--;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        return jsonArray;
    }

    private static boolean isOld(String date) {
        boolean res = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date messageDate = dateFormat.parse(date);
            Date now = new Date();
            long difference = now.getTime() - messageDate.getTime();
            if( (difference / (1000*60*60*24)) > DATE_THRESHOLD) { //30 days
                res = true;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not parse date!");
            Log.e(LOG_TAG, e.getMessage());
        }
        return res;
    }

    private static void createAndSaveNewOne(Context context, Message message) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            message.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            jsonArray.put(new JSONObject(new Gson().toJson(message)));
            jsonObject.put(Constants.PAYLOAD_SP_ARRAY_KEY, jsonArray);
            SharedPreference.saveString(context, Constants.PAYLOAD_SP_KEY, jsonObject.toString());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not save the push message!");
            Log.e(LOG_TAG, e.getMessage());
        }
    }
}
