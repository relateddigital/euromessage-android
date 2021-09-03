package com.relateddigital.euromessage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import euromsg.com.euromobileandroid.model.CarouselItem;
import euromsg.com.euromobileandroid.utils.LogUtils;

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
                        StackTraceElement element = new Throwable().getStackTrace()[0];
                        LogUtils.formGraylogModel(
                                context,
                                "e",
                                "Carousel item target link : " + e.getMessage(),
                                element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                        );
                        Log.e(LOG_TAG, "The link is not formatted properly!");
                    }
                    context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                }
                /*Message message = (Message) intent.getSerializableExtra("message");

                String intentStr = SharedPreference.getString(context, Constants.INTENT_NAME);

                if (!intentStr.isEmpty()) {
                    try {
                        intent = new Intent(context, Class.forName(intentStr));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("message", message);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                } else {
                    intent = AppUtils.getLaunchIntent(context, message);
                }

                context.startActivity(intent);
                context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));*/
            }
        }
    }
}
