package com.relateddigital.euromessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import euromsg.com.euromobileandroid.carousalnotification.Carousal;
import euromsg.com.euromobileandroid.carousalnotification.CarousalItem;


public class CarousalItemClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            //Intent intent1 = new Intent(context, MainActivity.class);
            //intent1.putExtras(bundle);
            //intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(intent1);
            CarousalItem item = bundle.getParcelable(Carousal.CAROUSAL_ITEM_CLICKED_KEY);
            if (item != null) {
                //Now we need to know where to redirect event
                if (!TextUtils.isEmpty(item.getType())) {
                            Intent detailIntent = new Intent(context, MainActivity.class);
                            Bundle b = new Bundle();
                           // b.putString(M.QUOTE_ID_KEY, item.getId());
                            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            detailIntent.putExtras(b);
                            context.startActivity(detailIntent);
                    }
                }
            } else {  //Meaning other region is clicked and isOtherRegionClick is set to true.
                Toast.makeText(context, "Other region clicked", Toast.LENGTH_LONG).show();
            }

        }
}
