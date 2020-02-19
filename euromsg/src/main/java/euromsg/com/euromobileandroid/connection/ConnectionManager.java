package euromsg.com.euromobileandroid.connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
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

    public Bitmap getBitmap(final String urlString) {
        try {
            return BitmapFactory.decodeStream(new URL(urlString).openConnection().getInputStream());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
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

    private static boolean makeJsonRequest(BaseRequest requestModel, String urlString) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        int responseCode = 0;
        try {
            URL url = new URL(urlString);
            String message = gson.toJson(requestModel);
            EuroLogger.debugLog("Request to : " + requestModel.getClass().getName() + " with : " + message);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connectionTimeout);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);
            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            //open
            conn.connect();
            //setup send
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            //clean up
            os.flush();
            responseCode = conn.getResponseCode();
        } catch (Exception e) {
            EuroLogger.debugLog(e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                EuroLogger.debugLog(e.getMessage());
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        //EuroLogger.debugLog("Request for : " + requestModel.getClass().getName() + " Server response : " + responseCode);
        return responseCode > 199 && responseCode < 300;
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
