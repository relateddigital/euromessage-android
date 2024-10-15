package euromsg.com.euromobileandroid.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;

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
                if(isPushIdAvailable(context, jsonArray, message)) {
                    return;
                }
                jsonArray = addNewOne(context, jsonArray, message);
                if(jsonArray == null) {
                    return;
                }
                jsonArray = removeOldOnes(context, jsonArray);
                JSONObject finalObject = new JSONObject();
                finalObject.put(Constants.PAYLOAD_SP_ARRAY_KEY, jsonArray);
                SharedPreference.saveString(context, Constants.PAYLOAD_SP_KEY, finalObject.toString());
            } catch (Exception e) {
                StackTraceElement element = new Throwable().getStackTrace()[0];
                LogUtils.formGraylogModel(
                        context,
                        "e",
                        "Serializing push message : " + e.getMessage(),
                        element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                );
                Log.e(LOG_TAG, "Something went wrong when adding the push message to shared preferences!");
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
            createAndSaveNewOne(context, message);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void addPushMessageWithId(Context context, Message message, String loginID) {
        String payloads = SharedPreference.getString(context, Constants.PAYLOAD_SP_ID_KEY);
        if(!payloads.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payloads);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.PAYLOAD_SP_ARRAY_ID_KEY);
                if(isPushIdAvailable(context, jsonArray, message)) {
                    return;
                }
                jsonArray = addNewOneWithID(context, jsonArray, message, loginID);
                if(jsonArray == null) {
                    return;
                }
                jsonArray = removeOldOnes(context, jsonArray);
                JSONObject finalObject = new JSONObject();
                finalObject.put(Constants.PAYLOAD_SP_ARRAY_ID_KEY, jsonArray);
                SharedPreference.saveString(context, Constants.PAYLOAD_SP_ID_KEY, finalObject.toString());
            } catch (Exception e) {
                StackTraceElement element = new Throwable().getStackTrace()[0];
                LogUtils.formGraylogModel(
                        context,
                        "e",
                        "Serializing push message : " + e.getMessage(),
                        element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                );
                Log.e(LOG_TAG, "Something went wrong when adding the push message to shared preferences!");
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
            createAndSaveNewOneWithID(context, message, loginID);
        }
    }

    public static List<Message> orderPushMessages(Context context, List<Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            for (int j = 0; j < messages.size() - 1 - i; j++) {
                if(compareDates(context, messages.get(j).getDate(), messages.get(j+1).getDate())) {
                    Message temp = messages.get(j);
                    messages.set(j, messages.get(j+1));
                    messages.set(j+1, temp);
                }
            }
        }
        return messages;
    }

    private static boolean isPushIdAvailable(Context context, JSONArray jsonArray, Message message) {
        boolean res = false;
        for(int i = 0 ; i < jsonArray.length() ; i++) {
            try {
                if (jsonArray.getJSONObject(i).getString("pushId").equals(message.getPushId())){
                    res = true;
                    break;
                }
            } catch (Exception e) {
                StackTraceElement element = new Throwable().getStackTrace()[0];
                LogUtils.formGraylogModel(
                        context,
                        "e",
                        "Getting pushId from JSONArray : " + e.getMessage(),
                        element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                );
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        return res;
    }

    private static JSONArray addNewOne(Context context, JSONArray jsonArray, Message message){
        try {
            message.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            message.setStatus("D");
            message.setOpenDate("");
            Map<String, Object> userExVid = EuroMobileManager.getInstance().subscription.getExtra();
            if (userExVid.containsKey(Constants.EURO_USER_KEY) && userExVid.get(Constants.EURO_USER_KEY) != null) {
                Object keyID = userExVid.get(Constants.EURO_USER_KEY);
                if(keyID instanceof String) {
                    message.setKeyID(keyID.toString());
                }
            }
            if (userExVid.containsKey(Constants.EURO_EMAIL_KEY) && userExVid.get(Constants.EURO_EMAIL_KEY) != null) {
                Object email = userExVid.get(Constants.EURO_EMAIL_KEY);
                if(email instanceof String) {
                    message.setEmail(email.toString());
                }
            }
            jsonArray.put(new JSONObject(new Gson().toJson(message)));
            return jsonArray;
        } catch (Exception e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Serializing push message : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            Log.e(LOG_TAG, "Could not save the push message!");
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    private static JSONArray addNewOneWithID(Context context, JSONArray jsonArray, Message message,
                                             String loginID){
        try {
            message.setLoginID(loginID);
            message.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            message.setStatus("D");
            message.setOpenDate("");
            Map<String, Object> userExVid = EuroMobileManager.getInstance().subscription.getExtra();
            if (userExVid.containsKey(Constants.EURO_USER_KEY) && userExVid.get(Constants.EURO_USER_KEY) != null) {
                Object keyID = userExVid.get(Constants.EURO_USER_KEY);
                if(keyID instanceof String) {
                    message.setKeyID(keyID.toString());
                }
            }
            if (userExVid.containsKey(Constants.EURO_EMAIL_KEY) && userExVid.get(Constants.EURO_EMAIL_KEY) != null) {
                Object email = userExVid.get(Constants.EURO_EMAIL_KEY);
                if(email instanceof String) {
                    message.setEmail(email.toString());
                }
            }
            jsonArray.put(new JSONObject(new Gson().toJson(message)));
            return jsonArray;
        } catch (Exception e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Serializing push message : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            Log.e(LOG_TAG, "Could not save the push message!");
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static JSONArray removeOldOnes(Context context, JSONArray jsonArray) {
        for(int i = 0 ; i < jsonArray.length() ; i++) {
            try {
                if (isOld(context, jsonArray.getJSONObject(i).getString("date"))){
                    jsonArray.remove(i);
                    i--;
                }
            } catch (Exception e) {
                StackTraceElement element = new Throwable().getStackTrace()[0];
                LogUtils.formGraylogModel(
                        context,
                        "e",
                        "Removing push message from JSONArray : " + e.getMessage(),
                        element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                );
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        return jsonArray;
    }

    private static boolean isOld(Context context, String date) {
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
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Comparing 2 dates : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
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
            message.setStatus("D");
            message.setOpenDate("");
            Map<String, Object> userExVid = EuroMobileManager.getInstance().subscription.getExtra();
            if (userExVid.containsKey(Constants.EURO_USER_KEY) && userExVid.get(Constants.EURO_USER_KEY) != null) {
                Object keyID = userExVid.get(Constants.EURO_USER_KEY);
                if(keyID instanceof String) {
                    message.setKeyID(keyID.toString());
                }
            }
            if (userExVid.containsKey(Constants.EURO_EMAIL_KEY) && userExVid.get(Constants.EURO_EMAIL_KEY) != null) {
                Object email = userExVid.get(Constants.EURO_EMAIL_KEY);
                if(email instanceof String) {
                    message.setEmail(email.toString());
                }
            }
            jsonArray.put(new JSONObject(new Gson().toJson(message)));
            jsonObject.put(Constants.PAYLOAD_SP_ARRAY_KEY, jsonArray);
            SharedPreference.saveString(context, Constants.PAYLOAD_SP_KEY, jsonObject.toString());
        } catch (Exception e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Forming and serializing push message string : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            Log.e(LOG_TAG, "Could not save the push message!");
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private static void createAndSaveNewOneWithID(Context context, Message message, String loginID) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            message.setLoginID(loginID);
            message.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            message.setStatus("D");
            message.setOpenDate("");
            Map<String, Object> userExVid = EuroMobileManager.getInstance().subscription.getExtra();
            if (userExVid.containsKey(Constants.EURO_USER_KEY) && userExVid.get(Constants.EURO_USER_KEY) != null) {
                Object keyID = userExVid.get(Constants.EURO_USER_KEY);
                if(keyID instanceof String) {
                    message.setKeyID(keyID.toString());
                }
            }
            if (userExVid.containsKey(Constants.EURO_EMAIL_KEY) && userExVid.get(Constants.EURO_EMAIL_KEY) != null) {
                Object email = userExVid.get(Constants.EURO_EMAIL_KEY);
                if(email instanceof String) {
                    message.setEmail(email.toString());
                }
            }
            jsonArray.put(new JSONObject(new Gson().toJson(message)));
            jsonObject.put(Constants.PAYLOAD_SP_ARRAY_ID_KEY, jsonArray);
            SharedPreference.saveString(context, Constants.PAYLOAD_SP_ID_KEY, jsonObject.toString());
        } catch (Exception e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Forming and serializing push message string : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            Log.e(LOG_TAG, "Could not save the push message!");
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public static void updatePayload(Context context, String pushId) {
        try {
            String jsonString = SharedPreference.getString(context, Constants.PAYLOAD_SP_KEY);
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray payloadsArray = jsonObject.optJSONArray(Constants.PAYLOAD_SP_ARRAY_KEY);

            if (payloadsArray != null) {
                for (int i = 0; i < payloadsArray.length(); i++) {
                    JSONObject payloadObject = payloadsArray.getJSONObject(i);
                    String existingPushId = payloadObject.optString("pushId", "");

                    if (existingPushId.equals(pushId)) {
                        // Güncelleme işlemlerini yap
                        payloadObject.put("status", "O");
                        payloadObject.put("openDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                        // Güncellenmiş JSON'ı kaydet
                        SharedPreference.saveString(context, Constants.PAYLOAD_SP_KEY, jsonObject.toString());
                        return; // Güncelleme işlemi tamamlandı, fonksiyondan çık
                    }
                }

                // Eğer bu noktaya gelinirse, belirtilen pushId ile bir payload bulunamamıştır.
                Log.e(LOG_TAG, "Payload with pushId " + pushId + " not found!");
            } else {
                Log.e(LOG_TAG, "Payload array is null or empty!");
            }
        } catch (JSONException e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Updating push message string : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            Log.e(LOG_TAG, "Could not update the push message!");
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public static void updatePayloadWithId(Context context, String pushId, Integer notificationId) {
        try {
            String jsonString = SharedPreference.getString(context, Constants.PAYLOAD_SP_KEY);
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray payloadsArray = jsonObject.optJSONArray(Constants.PAYLOAD_SP_ARRAY_KEY);

            if (payloadsArray != null) {
                for (int i = 0; i < payloadsArray.length(); i++) {
                    JSONObject payloadObject = payloadsArray.getJSONObject(i);
                    String existingPushId = payloadObject.optString("pushId", "");

                    if (existingPushId.equals(pushId)) {
                        payloadObject.put("notificationId", notificationId);


                        SharedPreference.saveString(context, Constants.PAYLOAD_SP_KEY, jsonObject.toString());
                        return;
                    }
                }


                Log.e(LOG_TAG, "Payload with pushId " + pushId + " not found!");
            } else {
                Log.e(LOG_TAG, "Payload array is null or empty!");
            }
        } catch (JSONException e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Updating push message string : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            Log.e(LOG_TAG, "Could not update the push message!");
            Log.e(LOG_TAG, e.getMessage());
        }
    }



    private static boolean compareDates(Context context, String str1, String str2) {
        boolean res = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date1 = dateFormat.parse(str1);
            Date date2 = dateFormat.parse(str2);
            if((date1.getTime()-date2.getTime()) < 0) {
                res = true;
            }
        } catch (Exception e) {
            StackTraceElement element = new Throwable().getStackTrace()[0];
            LogUtils.formGraylogModel(
                    context,
                    "e",
                    "Comparing 2 dates : " + e.getMessage(),
                    element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
            );
            Log.e(LOG_TAG, "Could not parse date!");
            Log.e(LOG_TAG, e.getMessage());
        }
        return res;
    }
}
