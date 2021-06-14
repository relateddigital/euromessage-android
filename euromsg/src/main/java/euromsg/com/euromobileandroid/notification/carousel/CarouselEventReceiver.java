package euromsg.com.euromobileandroid.notification.carousel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
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
                sendOpenReport(bundle, context);
            }

            if (carouselEvent > 0 && carousel != null)
                CarouselBuilder.with(context, notificationId).handleClickEvent(carouselEvent, carousel);
        }
    }

    private void sendOpenReport(Bundle bundle, Context context) {
        if(EuroMobileManager.getInstance() == null) {
            String appAlias = SharedPreference.getString(context, euromsg.com.euromobileandroid.Constants.GOOGLE_APP_ALIAS);
            String huaweiAppAlias = SharedPreference.getString(context, Constants.HUAWEI_APP_ALIAS);
            EuroMobileManager.init(appAlias, huaweiAppAlias, context).reportRead(bundle);
        } else {
            EuroMobileManager.getInstance().reportRead(bundle);
        }
    }
}