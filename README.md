
<p align="center">
  <a target="_blank" rel="noopener noreferrer" href="https://github.com/relateddigital/euromessage-android"><img src="https://www.tsoftapps.com/Data/Logo/euromsg.svg" alt="Euromessage Android Library" width="600" style="max-width:100%;"></a>
</p>


***July 24, 2020*** - [Euromessage v4.1.0](https://github.com/relateddigital/euromessage-android/releases/tag/4.1.0)

 **Bintray** [ ![Bintray Maven Download](https://api.bintray.com/packages/visilabs/euromessage/euromessage/images/download.svg) ](https://bintray.com/visilabs/euromessage/euromessage/_latestVersion)

# Table of Contents

- [Table of Contents](#table-of-contents)
- [Euromessage Android](#euromessage-android)
  * [1.Installation](#1installation)
    + [Gradle](#gradle)
  * [2.Usage of the SDK](#2usage-of-the-sdk)
    + [Initialization](#initialization)
  * [3.Sync](#3sync)
  * [4.Sample Applications](#4sample-applications)
  * [5.Licences](#5licences)


# Euromessage Android

The Euromessage Android Sdk is a java implementation of an Android client for Euromessage.

For more information, please check new detailed documentation :

[Euromessage English Documentation](https://relateddigital.atlassian.net/wiki/spaces/KB/pages/428966369/ANDROID+SDK)

[Euromessage Türkçe Dökümantasyon](https://relateddigital.atlassian.net/wiki/spaces/RMCKBT/pages/428802131/ANDROID+SDK)


### Notifications
<img src="https://github.com/relateddigital/euromessage-android/blob/master/notification.gif" alt="Euromessage Android Library" width="300" style="max-width:100%;">

## 1.Installation

### Gradle

Add Euromessage to the ```dependencies``` in app/build.gradle.

```java
implementation 'com.euromsg:euromsg:4.1.0'
```

Also you can check our sample build.gradle to installation
 [Project build.gradle](https://github.com/relateddigital/euromessage-android/blob/master/build.gradle)
 [App  build.gradle](https://github.com/relateddigital/euromessage-android/blob/master/app/build.gradle)

 
###### Note :
You need to add an android project in [Firebase Console](https://console.firebase.google.com/). Please follow Firebase instruction and do not forget to add google_service.json to the project.


You need to add an android project in [Huawei Console](https://developer.huawei.com/consumer/en/console). Please follow Huawei instruction and do not forget to add ag_connect_services.json to the project.
*you may need to add your fingerprint to app in Huawei console.


## 2.Usage of the SDK.  
 
 ### Initialization
 
Android Manifest
    
    <service
            android:name="euromsg.com.euromobileandroid.service.EuroFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <service
            android:name="euromsg.com.euromobileandroid.service.EuroHuaweiMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.huawei.push.action.MESSAGING_EVENT" />
            </intent-filter>
        </service>

Main Application 

```java

 EuroMobileManager euroMobileManager = EuroMobileManager.init("euromessage-android", "euromsg-huawei", getApplicationContext());

  euroMobileManager.registerToFCM(getBaseContext());   
  ```
  --
  

  Huawei :  
  
        if (!EuroMobileManager.checkPlayService(getApplicationContext())) {
            setHuaweiTokenToEuromessage();
        }
        
One feature of Huawei Push Kit is that it can not collect all tokens on onNewToken in HMSMessaging Class, 
According to EMUI, the way to get tokens is also changing. When collecting token from onNewToken, Euromessage will do it for you.


EMUI 10+ will get token with a code piece in your class. After make sure that generate the Huawei token, you may subscribe it to Euromeesage SDK 

          private void setHuaweiTokenToEuromessage() {

        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(getApplicationContext()).getString("client/app_id");
                    String token = HmsInstanceId.getInstance(getApplicationContext()).getToken(appId, "HCM");

                    euroMobileManager.subscribe(token, getApplicationContext());

                    Log.i("Huawei Token", "" + token);

                } catch (ApiException e) {
                    Log.e("Huawei Token", "get token failed, " + e);
                }
            }
        }.start();
    }
    
    
    
   If you want to subscribe existing firebase token to euromessage, you need to add  : 
    
 
     private void setExistingFirebaseTokenToEuroMessage() {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (!task.isSuccessful()) {
                            return;
                        }

                        String token = task.getResult().getToken();
                        euroMobileManager.subscribe(token, getApplicationContext());
                    }
                });
    }
  
    
  RMC needs that at least one property which is email or user id in order to match and analyse users. This is important part to use RMC.  Please add code below after being sure you generate the token.. 

  ``` 
  euroMobileManager.setEmail("test@mail.com", this);
  euroMobileManager.setEuroUserId("12345", this);
  euroMobileManager.sync(this);
   ```
   
   RMC needs that information about the notification is read by user. You need to add code below.
   
       @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras() != null) {
            euroMobileManager.reportRead(intent.getExtras());
        }
    }
          
      
***Warning :***   
In some states, intent can be null, Please make sure that when you set reportRead intent to euromessage.


    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getExtras() != null && euroMobileManager.getNotification(getIntent()) != null) {
            euroMobileManager.reportRead(getIntent().getExtras());
        }

    }
    

 ## 3.Sync
 
 Please make the following improvements to add more information about the user in RMC.
 
```java
 
EuroMobileManager manager = EuroMobileManager.getInstance();
 
manager.setEmail("melike.yildirim@euromsg.com", this);
manager.setEuroUserId("2342343", this);
manager.setFacebook("euroFB", this);
manager.setTwitterId("euroTW", this);
manager.setPhoneNumber("05320000000", this);
manager.sync();

```

## 4.Sample Applications 

- [Euromessage Sample Application](https://github.com/relateddigital/euromessage-android/releases/tag/3.0.1) 
 (master branch)

- [Euromessage Sample Application - (Support Library) ](https://github.com/relateddigital/euromessage-android/tree/euromessage-support)

- If you have a question please send an e-mail to: <clientsupport@relateddigital.com> 

## 5.Licences

 - [Related Digital ](https://www.relateddigital.com/)
 - [Euromessage](https://www.euromsg.com/)
