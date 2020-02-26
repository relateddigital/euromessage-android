package euromsg.com.euromobileandroid.connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import euromsg.com.euromobileandroid.BuildConfig;
import euromsg.com.euromobileandroid.model.BaseRequest;
import euromsg.com.euromobileandroid.model.Retention;
import euromsg.com.euromobileandroid.model.Subscription;
import euromsg.com.euromobileandroid.utils.EuroLogger;
public final class ConnectionManager {

    private static final int connectionTimeout = 15000;
    private static final int readTimeout = 10000;

    private static ConnectionManager instance;

    public static ConnectionManager getInstance() {
        return instance;
    }

    static {
        instance = new ConnectionManager();
    }

    private ConnectionManager() {
    }

    public void get(final String urlString) {
        new GetAsyncTask(urlString).execute();
    }

    public Bitmap getBitMapFromUri(String photo_url) {

        URL url;

        Bitmap image = null;
        try {
            url = new URL(photo_url);

            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public void subscribe(final Subscription subscription) {
        new JsonAsyncTask(subscription, "https://test.euromsg.com:4242/subscription").execute();
       // new JsonAsyncTask(subscription, "https://pushs.euromsg.com/subscription").execute();
    }

    public void report(final Retention retention) {
       // new JsonAsyncTask(retention, "https://pushr.euromsg.com/retention").execute();
        new JsonAsyncTask(retention, "https://test.euromsg.com:4243/retention").execute();
    }

    private static Gson gson = new Gson();

    private static void makeJsonRequest(BaseRequest requestModel, String urlString) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        int responseCode;
        try {
            URL url = new URL(urlString);
            String message = gson.toJson(requestModel);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connectionTimeout);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.connect();
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            os.flush();
            responseCode = conn.getResponseCode();

            EuroLogger.debugLog("Request for : " + requestModel.getClass().getName() + "with : " + message + " Server response : " + responseCode);

        } catch (Exception e) {
            EuroLogger.debugLog(e.toString());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                EuroLogger.debugLog(e.toString());
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static class GetAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<String> urlStringReference;

        GetAsyncTask(String urlString){
            urlStringReference = new WeakReference<>(urlString);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) new URL(urlStringReference.get()).openConnection();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

    private static class JsonAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<String> requestUrlWeakReference;
        private WeakReference<BaseRequest> requestWeakReference;

        JsonAsyncTask(BaseRequest baseRequest, String requestUrl){
            requestUrlWeakReference = new WeakReference<>(requestUrl);
            requestWeakReference = new WeakReference<>(baseRequest);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            makeJsonRequest(requestWeakReference.get(), requestUrlWeakReference.get());
            return null;
        }
    }
}