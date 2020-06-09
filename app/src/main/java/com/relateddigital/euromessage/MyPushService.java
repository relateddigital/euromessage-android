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

package com.relateddigital.euromessage;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.push.SendException;

import java.util.Arrays;

public class MyPushService extends HmsMessageService {

    private static final String TAG = "Huawei token";

    @Override
    public void onNewToken(String token) {

        //Google service check

        Log.i("Huawei Token", "Huawei Token refresh token:" + token);

        if (!TextUtils.isEmpty(token)) {
            refreshedTokenToServer(token);
        }

        Intent intent = new Intent();
        intent.setAction("com.relateddigital.euromessage.MainActivity");
        intent.putExtra("method", "onNewToken");
        intent.putExtra("msg", "onNewToken called, token: " + token);

        sendBroadcast(intent);
    }

    private void refreshedTokenToServer(String token) {
        Log.i(TAG, "sending token to server. token:" + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.i(TAG, "onMessageReceived is called");
        if (message == null) {
            Log.e(TAG, "Received message entity is null!");
            return;
        }

        Log.i(TAG, "getCollapseKey: " + message.getCollapseKey()
                + "\n getData: " + message.getData()
                + "\n getFrom: " + message.getFrom()
                + "\n getTo: " + message.getTo()
                + "\n getMessageId: " + message.getMessageId()
                + "\n getOriginalUrgency: " + message.getOriginalUrgency()
                + "\n getUrgency: " + message.getUrgency()
                + "\n getSendTime: " + message.getSentTime()
                + "\n getMessageType: " + message.getMessageType()
                + "\n getTtl: " + message.getTtl());

        RemoteMessage.Notification notification = message.getNotification();
        if (notification != null) {
            Log.i(TAG, "\n getImageUrl: " + notification.getImageUrl()
                    + "\n getTitle: " + notification.getTitle()
                    + "\n getTitleLocalizationKey: " + notification.getTitleLocalizationKey()
                    + "\n getTitleLocalizationArgs: " + Arrays.toString(notification.getTitleLocalizationArgs())
                    + "\n getBody: " + notification.getBody()
                    + "\n getBodyLocalizationKey: " + notification.getBodyLocalizationKey()
                    + "\n getBodyLocalizationArgs: " + Arrays.toString(notification.getBodyLocalizationArgs())
                    + "\n getIcon: " + notification.getIcon()
                    + "\n getSound: " + notification.getSound()
                    + "\n getTag: " + notification.getTag()
                    + "\n getColor: " + notification.getColor()
                    + "\n getClickAction: " + notification.getClickAction()
                    + "\n getChannelId: " + notification.getChannelId()
                    + "\n getLink: " + notification.getLink()
                    + "\n getNotifyId: " + notification.getNotifyId());
        }

        Intent intent = new Intent();
        intent.setAction("com.relateddigital.euromessage.MainActivity");
        intent.putExtra("method", "onMessageReceived");
        intent.putExtra("msg", "onMessageReceived called, message id:" + message.getMessageId() + ", payload data:"
                + message.getData());

        sendBroadcast(intent);

        Boolean judgeWhetherIn10s = false;

        // If the messages are not processed in 10 seconds, the app needs to use WorkManager for processing.
        if (judgeWhetherIn10s) {
            startWorkManagerJob(message);
        } else {
            // Process message within 10s
            processWithin10s(message);
        }
    }

    private void startWorkManagerJob(RemoteMessage message) {
        Log.d(TAG, "Start new Job processing.");
    }

    private void processWithin10s(RemoteMessage message) {
        Log.d(TAG, "Processing now.");
    }

    @Override
    public void onMessageSent(String msgId) {
        Log.i(TAG, "onMessageSent called, Message id:" + msgId);
        Intent intent = new Intent();
        intent.setAction("com.relateddigital.euromessage.MainActivity");
        intent.putExtra("method", "onMessageSent");
        intent.putExtra("msg", "onMessageSent called, Message id:" + msgId);

        sendBroadcast(intent);
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        Log.i(TAG, "onSendError called, message id:" + msgId + ", ErrCode:"
                + ((SendException) exception).getErrorCode() + ", description:" + exception.getMessage());

        Intent intent = new Intent();
        intent.setAction("com.relateddigital.euromessage.MainActivity");
        intent.putExtra("method", "onSendError");
        intent.putExtra("msg", "onSendError called, message id:" + msgId + ", ErrCode:"
            + ((SendException) exception).getErrorCode() + ", description:" + exception.getMessage());

        sendBroadcast(intent);
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);
    }
}
