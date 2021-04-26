package com.relateddigital.euromessage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import euromsg.com.euromobileandroid.model.CarouselItem;
import static euromsg.com.euromobileandroid.Constants.CAROUSAL_ITEM_CLICKED_KEY;
import static euromsg.com.euromobileandroid.Constants.CAROUSEL_ITEM_CLICKED_URL;

public class CarouselItemClickReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "CarouselItemReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                CarouselItem itemClicked = (CarouselItem) bundle.getParcelable(CAROUSAL_ITEM_CLICKED_KEY);
                String itemClickedUrl = bundle.getString(CAROUSEL_ITEM_CLICKED_URL);
                if(itemClickedUrl != null && !itemClickedUrl.equals("")) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemClickedUrl));
                        viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(viewIntent);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "The link is not formatted properly!");
                    }
                }
            }
        }
    }
}
