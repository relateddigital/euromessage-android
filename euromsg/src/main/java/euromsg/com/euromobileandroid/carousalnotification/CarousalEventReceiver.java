package euromsg.com.euromobileandroid.carousalnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import euromsg.com.euromobileandroid.carousalnotification.Carousal;
import euromsg.com.euromobileandroid.carousalnotification.CarousalConstants;
import euromsg.com.euromobileandroid.carousalnotification.CarousalSetUp;

public class CarousalEventReceiver extends BroadcastReceiver {

    public Class activity;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int carousalEvent = bundle.getInt(CarousalConstants.EVENT_CAROUSAL_ITEM_CLICKED_KEY);
            CarousalSetUp carousalSetUp = bundle.getParcelable(CarousalConstants.CAROUSAL_SET_UP_KEY);

            Intent intent1 = new Intent(context, getActivity());

            switch (carousalEvent){

                case 3:
                    context.startActivity(intent1);
                    context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

                    break;

                case 4:

                    context.startActivity(intent1);
                    context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

                    break;
            }
            //Respond only if both things are there
            if (carousalEvent > 0 && carousalSetUp != null)
                Carousal.with(context).handleClickEvent(carousalEvent, carousalSetUp);
        }
    }

    public Class getActivity() {
        return activity;
    }

    public void setActivity(Class activity) {
        this.activity = activity;
    }
}