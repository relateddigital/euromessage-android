package euromsg.com.euromobileandroid.carousalnotification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class BasicImageDownloader {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private ArrayList<CarousalItem> carousalItems;
    private OnDownloadsCompletedListener onDownloadsCompletedListener;
    private int numberOfImages;
    private static int currentDownloadTaskIndex = 0;
    private CarousalItem currentItem;

    BasicImageDownloader(Context context, ArrayList<CarousalItem> carousalItems, int numberOfImages,
                         @NonNull OnDownloadsCompletedListener onDownloadsCompletedListener) {
        this.carousalItems = carousalItems;
        this.context = context;
        this.onDownloadsCompletedListener = onDownloadsCompletedListener;
        this.numberOfImages = numberOfImages;
    }

    private OnImageLoaderListener mImageLoaderListener = new OnImageLoaderListener() {
        @Override
        public void onError(ImageError error) {
            updateDownLoad(null);

        }

        @Override
        public void onComplete(String resultPath) {
            updateDownLoad(resultPath);

        }
    };

    private void updateDownLoad(String filePath) {

        for (int i = (currentDownloadTaskIndex + 1); i < carousalItems.size(); i++) {
            if (!TextUtils.isEmpty(carousalItems.get(i).getPhotoUrl())) {
                currentDownloadTaskIndex = i;
                currentItem = carousalItems.get(i);
                downloadImage(currentItem.getPhotoUrl());
                break;
            }
        }
        --numberOfImages;
        if (numberOfImages < 1 || currentDownloadTaskIndex > carousalItems.size() - 1) {
            onDownloadsCompletedListener.onComplete();
        }
    }

    void startAllDownloads() {
        if (carousalItems != null && carousalItems.size() > 0) {
            for (int i = 0; i < carousalItems.size(); i++) {
                if (!TextUtils.isEmpty(carousalItems.get(i).getPhotoUrl())) {
                    currentDownloadTaskIndex = i;
                    currentItem = carousalItems.get(i);
                    downloadImage(currentItem.getPhotoUrl());
                    break;
                }
            }
        }
    }

    public interface OnImageLoaderListener {
        void onError(ImageError error);

        void onComplete(String resultPath);
    }

    public interface OnDownloadsCompletedListener {

        void onComplete();
    }

    private Bitmap getBitMapFromUri(String photo_url) {

        URL url;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        Bitmap image = null;
        try {
            url = new URL(photo_url);

            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }


    private void downloadImage(@NonNull final String imageUrl) {

        new AsyncTask<Void, Integer, String>() {

            private ImageError error;
            private long currentTimeInMillis;


            @Override
            protected void onCancelled() {
                mImageLoaderListener.onError(error);
            }

            @Override
            protected String doInBackground(Void... params) {
                currentTimeInMillis = System.currentTimeMillis();
                Bitmap bitmap;
                String imagePath = null;

                try {

                    bitmap = getBitMapFromUri(imageUrl);

                    if (bitmap != null) {

                        int sampleSize = CarousalUtilities.carousalCalculateInSampleSize(bitmap.getWidth(), bitmap.getHeight(), 250, 250);
                        Bitmap bit = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / sampleSize, bitmap.getHeight() / sampleSize, false);
                        imagePath = CarousalUtilities.carousalSaveBitmapToInternalStorage(context, bit, CarousalConstants.CAROUSAL_IMAGE_BEGENNING + currentTimeInMillis);
                    }

                } catch (Throwable e) {
                    if (!this.isCancelled()) {
                        error = new ImageError(e).setErrorCode(ImageError.ERROR_GENERAL_EXCEPTION);
                        this.cancel(true);
                    }
                }

                return imagePath;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    Log.e(TAG, "factory returned a null result");
                    mImageLoaderListener.onError(new ImageError("downloaded file could not be decoded as bitmap")
                            .setErrorCode(ImageError.ERROR_DECODE_FAILED));
                } else {
                    Log.d(TAG, "download complete");
                    if (currentItem != null) {
                        currentItem.setImageFileLocation(result);
                        currentItem.setImageFileName(CarousalConstants.CAROUSAL_IMAGE_BEGENNING + currentTimeInMillis);
                    }
                    mImageLoaderListener.onComplete(result);
                }
                System.gc();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public static final class ImageError extends Throwable {

        private int errorCode;

        static final int ERROR_GENERAL_EXCEPTION = -1;

        public static final int ERROR_INVALID_FILE = 0;

        static final int ERROR_DECODE_FAILED = 1;


        ImageError(@NonNull String message) {
            super(message);
        }

        ImageError(@NonNull Throwable error) {
            super(error.getMessage(), error.getCause());
            this.setStackTrace(error.getStackTrace());
        }

        ImageError setErrorCode(int code) {
            this.errorCode = code;
            return this;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }
}
