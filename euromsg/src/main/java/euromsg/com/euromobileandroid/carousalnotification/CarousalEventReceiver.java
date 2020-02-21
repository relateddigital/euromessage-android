package euromsg.com.euromobileandroid.carousalnotification;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class CarousalEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int carousalEvent = bundle.getInt(CarousalConstants.EVENT_CAROUSAL_ITEM_CLICKED_KEY);
            CarousalSetUp carousalSetUp = bundle.getParcelable(CarousalConstants.CAROUSAL_SET_UP_KEY);

            PackageManager packageManager = context.getPackageManager();
            Intent a = packageManager.getLaunchIntentForPackage(context.getPackageName());
            ComponentName componentName = a.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);


            if (carousalEvent > 2) {
                context.startActivity(mainIntent);
                context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }

            if (carousalEvent > 0 && carousalSetUp != null)
                Carousal.with(context).handleClickEvent(carousalEvent, carousalSetUp);
        }
    }
}