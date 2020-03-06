package euromsg.com.euromobileandroid.connection;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.model.CarouselItem;
import euromsg.com.euromobileandroid.utils.ImageUtils;

public class ImageDownloaderManager {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private ArrayList<CarouselItem> carouselItems;
    private OnDownloadsCompletedListener onDownloadsCompletedListener;
    private int numberOfImages;
    private static int currentDownloadTaskIndex = 0;
    private CarouselItem currentItem;

    public ImageDownloaderManager(Context context, ArrayList<CarouselItem> carouselItems, int numberOfImages,
                                  @NonNull OnDownloadsCompletedListener onDownloadsCompletedListener) {
        this.carouselItems = carouselItems;
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

        for (int i = (currentDownloadTaskIndex + 1); i < carouselItems.size(); i++) {
            if (!TextUtils.isEmpty(carouselItems.get(i).getPhotoUrl())) {
                currentDownloadTaskIndex = i;
                currentItem = carouselItems.get(i);
                downloadImage(currentItem.getPhotoUrl());
                break;
            }
        }
        --numberOfImages;
        if (numberOfImages < 1 || currentDownloadTaskIndex > carouselItems.size() - 1) {
            onDownloadsCompletedListener.onComplete();
        }
    }

    public void startAllDownloads() {
        if (carouselItems != null && carouselItems.size() > 0) {
            for (int i = 0; i < carouselItems.size(); i++) {
                if (!TextUtils.isEmpty(carouselItems.get(i).getPhotoUrl())) {
                    currentDownloadTaskIndex = i;
                    currentItem = carouselItems.get(i);
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

                    bitmap = ConnectionManager.getInstance().getBitMapFromUri(imageUrl);

                    if (bitmap != null) {

                        int sampleSize = ImageUtils.calculateInSampleSize(bitmap.getWidth(), bitmap.getHeight(), 250, 250);
                        Bitmap bit = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / sampleSize, bitmap.getHeight() / sampleSize, false);
                        imagePath = ImageUtils.saveBitmapToInternalStorage(context, bit, Constants.CAROUSAL_IMAGE_BEGENNING + currentTimeInMillis);
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
                        currentItem.setImageFileName(Constants.CAROUSAL_IMAGE_BEGENNING + currentTimeInMillis);
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
