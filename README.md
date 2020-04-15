
<p align="center">
  <a target="_blank" rel="noopener noreferrer" href="https://github.com/relateddigital/euromessage-android"><img src="https://www.tsoftapps.com/Data/Logo/euromsg.svg" alt="Euromessage Android Library" width="600" style="max-width:100%;"></a>
</p>

# Latest Version [![Build Status](https://travis-ci.com/relateddigital/euromessage-android.svg?branch=master)](https://travis-ci.com/relateddigital/euromessage-android)

***April 10, 2020*** - [Euromessage v3.0.5](https://github.com/relateddigital/euromessage-android/releases/tag/3.0.5)

 **Bintray** [ ![Bintray Maven Download](https://api.bintray.com/packages/visilabs/euromessage/euromessage/images/download.svg) ](https://bintray.com/visilabs/euromessage/euromessage/_latestVersion)

# Table of Contents

- [Table of Contents](#table-of-contents)
- [Euromessage Android](#euromessage-android)
  * [1.Installation](#1installation)
    + [Gradle](#gradle)
    + [Permission](#permission)
  * [2.Usage of the SDK](#2usage-of-the-sdk)
    + [Initialization](#initialization)
      - [How to make configuration on RMC for AppAlias, Sound, Server Key?](#how-to-make-configuration-on-rmc-for-appalias--sound--server-key-)
  * [3.Sync](#3sync)
  * [4.Sample Applications](#4sample-applications)
  * [5.Licences](#5licences)


# Euromessage Android

The Euromessage Android Sdk is a java implementation of an Android client for Euromessage.

## 1.Installation

### Gradle

Add Euromessage to the ```dependencies``` in app/build.gradle.

```java
implementation 'com.euromsg:euromsg:3.0.5'
```
 
 
###### Note :
You need to add an android project in [Firebase Console](https://console.firebase.google.com/). Please follow Firebase instruction and do not forget to add google_service.json to the project.


#### Support Lib 

You may use [Euromessage Sdk](https://github.com/relateddigital/euromessage-android/tree/master/euromsg) directly.
  
  For that :
- Download the module
- Open your project which you want to use Euromessage
- Follow steps : Android Studio -> File -> New -> Import Module and select path where you want to locate module and rename it.



### Permission
```xml
     <uses-permission android:name="android.permission.INTER NET"/>

     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> 
 ```    
    

## 2.Usage of the SDK.  
 
 ### Initialization

```java

  public static String APP_ALIAS = Constant.APP_ALIAS;  // e.g.: "euromessage-android"

  EuroMobileManager euroMobileManager = EuroMobileManager.init(APP_ALIAS, this);

  euroMobileManager.registerToFCM(getBaseContext());   
  ```
  
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
            euroMobileManager.reportRead(new Message(intent.getExtras()));
        }
    }


#### How to make configuration on RMC for AppAlias, Sound, Server Key? 

You need to add configuration for your android application on RMC. Follow steps below to add an application.

 Setting Icon-> Campaing Managment -> Campaign Settings -> Push Applications 

<p align="center">
  <a target="_blank" rel="noopener noreferrer" href="https://github.com/relateddigital/euromessage-android/blob/master/ss.png"><img src="https://github.com/relateddigital/euromessage-android/blob/master/screenhelp.png" alt="Euromessage Android Library" width="600" style="max-width:100%;"></a>
</p>

- ***Server Key*** : It should be 'server key' from Firebase Cloud Messaging 
- ***App Alias*** : It can be any name eg: euromessagedemo
- ***Custom Sound File***  : It is an optional feature. It should be same name of a music file without extension in /raw folder 

 
 ## 3.Sync
 
 Please make the following improvements to add more information about the user in RMC.
 
```java
 
EuroMobileManager manager = EuroMobileManager.getInstance();
 
manager.setEmail("melike.yildirim@euromsg.com", this);
manager.setEuroUserId("2342343", this);
manager.setAppVersion("1.1");
manager.setFacebook("euroFB", this);
manager.setTwitterId(“euroTW", this);
manager.setPhoneNumber(“05320000000", this);
manager.sync();

```
### Notifications
<img src="https://github.com/relateddigital/euromessage-android/blob/master/notification.gif" alt="Euromessage Android Library" width="600" style="max-width:100%;">

## 4.Sample Applications 

- [Euromessage Sample Application](https://github.com/relateddigital/euromessage-android/releases/tag/3.0.1) 
 (master branch)

- [Euromessage Sample Application - (Support Library) ](https://github.com/relateddigital/euromessage-android/tree/euromessage-support)

- If you have a question please send an e-mail to: <clientsupport@relateddigital.com> 

## 5.Licences


 - [Related Digital ](https://www.relateddigital.com/)
 - [Euromessage](https://www.euromsg.com/)
