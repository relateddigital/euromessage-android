package euromsg.com.euromobileandroid.notification.carousel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.model.Carousel;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class CarouselEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int notificationId = bundle.getInt(Constants.NOTIFICATION_ID);
            int carouselEvent = bundle.getInt(Constants.EVENT_CAROUSAL_ITEM_CLICKED_KEY);
            Carousel carousel = bundle.getParcelable(Constants.CAROUSAL_SET_UP_KEY);

            if (carouselEvent > Constants.EVENT_RIGHT_ARROW_CLICKED) {

                Message message = (Message) intent.getSerializableExtra("message");

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
                context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }

            if (carouselEvent > 0 && carousel != null)
                CarouselBuilder.with(context, notificationId).handleClickEvent(carouselEvent, carousel);
        }
    }
}