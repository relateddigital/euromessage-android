/*
 *  Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package euromsg.com.euromobileandroid.service;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.push.SendException;

import java.util.Arrays;
import java.util.Map;

import euromsg.com.euromobileandroid.Constants;
import euromsg.com.euromobileandroid.EuroMobileManager;
import euromsg.com.euromobileandroid.model.Message;
import euromsg.com.euromobileandroid.notification.PushNotificationManager;
import euromsg.com.euromobileandroid.utils.AppUtils;
import euromsg.com.euromobileandroid.utils.EuroLogger;
import euromsg.com.euromobileandroid.utils.SharedPreference;

public class EuroHuaweiMessagingService extends HmsMessageService {

    private static final String TAG = "EuroMessage Huawei";

    @Override
    public void onNewToken(String token) {
        if (!checkPlayService()) {

            Log.i(TAG, "Huawei Token refresh token:" + token);
            EuroMobileManager.getInstance().subscribe(token, this);

            if (!TextUtils.isEmpty(token)) {
                refreshedTokenToServer(token);
                EuroMobileManager.getInstance().subscribe(token, this);
            }
        } else {
            Log.i(TAG, "Google Services Enable");
        }

    }

    private void refreshedTokenToServer(String token) {
        Log.i(TAG, "sending token to server. token:" + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (!checkPlayService()) {
            Log.i(TAG, "onMessageReceived is called");
            if (remoteMessage == null) {
                Log.e(TAG, "Received message entity is null!");
                return;
            }

            Message pushMessage = new Gson().fromJson(remoteMessage.getData(), Message.class);

            PushNotificationManager pushNotificationManager = new PushNotificationManager();

            EuroLogger.debugLog("Message received : " + pushMessage.getMessage());

            if (pushMessage.getPushType() != null && pushMessage.getPushId() != null) {

                switch (pushMessage.getPushType()) {

                    case Image:

                        if (pushMessage.getElements() != null) {
                            pushNotificationManager.generateCarouselNotification(this, pushMessage);
                        } else {
                            pushNotificationManager.generateNotification(this, pushMessage, AppUtils.getBitMapFromUri(pushMessage.getMediaUrl()));
                        }

                        break;

                    case Text:
                        pushNotificationManager.generateNotification(this, pushMessage, null);

                        break;

                    case Video:
                        break;

                    default:
                        pushNotificationManager.generateNotification(this, pushMessage, null);
                        break;
                }

                String appAlias = SharedPreference.getString(this, Constants.APP_ALIAS);

                EuroMobileManager.init(appAlias, this).reportReceived(pushMessage.getPushId());
            } else {
                EuroLogger.debugLog("remoteMessageData transfrom problem");
            }
        } else {
            Log.i(TAG, "Google Services are enable");
        }
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);

        Log.e(TAG, "Token is not generated" + e.toString());
    }


    private boolean checkPlayService() {
        boolean result = true;

        int isGoogleEnabled = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

        switch (isGoogleEnabled) {
            case ConnectionResult.API_UNAVAILABLE:
                Log.e(TAG, "Google API Unavailable");
                result = false;
                //API is not available
                break;

                case ConnectionResult.NETWORK_ERROR:
                Log.e(TAG, "Google Network Error");
                result = false;

                //Network error while connection
                break;

            case ConnectionResult.RESTRICTED_PROFILE:
                Log.e(TAG, "Google Restricted");
                result = false;

                //Profile is restricted by google so can not be used for play services
                break;

            case ConnectionResult.SERVICE_MISSING:
                //service is missing
                result = false;

                Log.e(TAG, "Google Service is missing");

                break;

            case ConnectionResult.SIGN_IN_REQUIRED:
                //service available but user not signed in
                Log.e(TAG, "Google Sign in req");
                result = false;

                break;
            case ConnectionResult.SERVICE_INVALID:
                Log.e(TAG, "Google Services invalid");
                result = false;

                //  The version of the Google Play services installed on this device is not authentic
                break;
            case ConnectionResult.SUCCESS:
                result = true;

                Log.e(TAG, "Google Services are Enable");

                break;
        }

        return result;
    }
}
