package euromsg.com.euromobileandroid.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.LogUtils;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class EuroMessageOpenReportService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public EuroMessageOpenReportService(String name) {
        super(name);
    }

    public EuroMessageOpenReportService() {
        super("EuroMessageOpenReportService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getExtras() != null) {
            Message message = (Message) intent.getExtras().getSerializable("message");
            //sendOpenReport(intent); //TODO : Open this line to enable sending read report from sdk
            startTheRelatedActivity(message);
        } else {
            Log.e("PushClick : ", "The payload is empty. The read report could not be sent!");
        }
    }

    private void sendOpenReport(Intent intent) {
        if(EuroMobileManager.getInstance() == null) {
            String appAlias = SharedPreference.getString(getApplicationContext(), Constants.GOOGLE_APP_ALIAS);
            String huaweiAppAlias = SharedPreference.getString(getApplicationContext(), Constants.HUAWEI_APP_ALIAS);
            EuroMobileManager.init(appAlias, huaweiAppAlias, getApplicationContext()).reportRead(intent.getExtras());
        } else {
            EuroMobileManager.getInstance().reportRead(intent.getExtras());
        }
    }

    private void startTheRelatedActivity(Message pushMessage) {
        String intentStr = SharedPreference.getString(getApplicationContext(), Constants.INTENT_NAME);

        Intent intent;
        if (!intentStr.isEmpty()) {
            try {
                intent = new Intent(getApplicationContext(), Class.forName(intentStr));
                intent.putExtra("message", pushMessage);
            } catch (Exception e) {
                StackTraceElement element = new Throwable().getStackTrace()[0];
                LogUtils.formGraylogModel(
                        this,
                        "e",
                        "Navigating to the activity of the customer : " + e.getMessage(),
                        element.getClassName() + "/" + element.getMethodName() + "/" + element.getLineNumber()
                );
                Log.e("PushClick : ", "The class could not be found!");
                intent = AppUtils.getLaunchIntent(getApplicationContext(), pushMessage);
            }

        } else {
            intent = AppUtils.getLaunchIntent(getApplicationContext(), pushMessage);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
