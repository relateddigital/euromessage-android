package euromsg.com.euromobileandroid.notification.carousel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.CarouselSetUp;
import euromsg.com.euromobileandroid.model.Element;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.utils.AppUtils;

public class CarouselEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int carouselEvent = bundle.getInt(Constants.EVENT_CAROUSAL_ITEM_CLICKED_KEY);
            CarouselSetUp carouselSetUp = bundle.getParcelable( Constants.CAROUSAL_SET_UP_KEY);

            if (carouselEvent > 2) {
                Message model = (Message) intent.getSerializableExtra("message");
                context.startActivity(AppUtils.getLaunchIntent(context, model));
                context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                EuroMobileManager.getInstance().reportRead(bundle);
            }

            if (carouselEvent > 0 && carouselSetUp != null)
                CarouselBuilder.with(context).handleClickEvent(carouselEvent, carouselSetUp);
        }
    }
}