
<p align="center">
  <a target="_blank" rel="noopener noreferrer" href="https://github.com/relateddigital/euromessage-android"><img src="https://www.tsoftapps.com/Data/Logo/euromsg.svg" alt="Euromessage Android Library" width="600" style="max-width:100%;"></a>
</p>


***October 13, 2023*** - [Euromessage v5.2.4](https://github.com/relateddigital/euromessage-android/releases/tag/5.2.4)

# Table of Contents

- [Table of Contents](#table-of-contents)
- [Euromessage Android](#euromessage-android)
  * [1.Installation](#1installation)
    + [Gradle](#gradle)
  * [2.Usage of the SDK](#2usage-of-the-sdk)
  * [3.Sample Applications](#4sample-applications)
  * [4.IYS Email Register](#4iys-email-register)
  * [5.Licences](#5licences)


# Euromessage Android

The Euromessage Android Sdk is a java implementation of an Android client for Euromessage.


### Notifications
<img src="https://github.com/relateddigital/euromessage-android/blob/master/notification.gif" alt="Euromessage Android Library" width="300" style="max-width:100%;">

## 1.Installation

Euromessage SDK requires minimum API level 21.

Add maven jitpack repository to your project/build.gradle file

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Add Euromessage to the ```dependencies``` in app/build.gradle.

```java
implementation 'com.github.relateddigital:euromessage-android:5.2.4'
```

## 2. Usage of SDK 

For more information, please check new detailed documentation :

[Euromessage Türkçe Dokümantasyon](https://relateddigital.atlassian.net/wiki/spaces/RMCKBT/pages/428802131/ANDROID+SDK)

[Euromessage English Documentation](https://relateddigital.atlassian.net/wiki/spaces/KB/pages/428966369/ANDROID+SDK)


 
###### Note :
You need to add an android project in [Firebase Console](https://console.firebase.google.com/). Please follow Firebase instruction and do not forget to add google_service.json to the project.


You need to add an android project in [Huawei Console](https://developer.huawei.com/consumer/en/console). Please follow Huawei instruction and do not forget to add agconnect_services.json to the project.
*you may need to add your fingerprint to app in Huawei console.


## 3.Sample Applications 


- [Euromessage Sample Application ](https://github.com/relateddigital/euromessage-android)
- [Euromessage Visilabs Shopping Application ](https://github.com/relateddigital/sample-shopping-android)

- If you have a question please send an e-mail to: <clientsupport@relateddigital.com> 

## 4.IYS Email Register

To register email IYS:

```java
EuromessageCallback callback = new EuromessageCallback() {
    @Override
    public void success() {
        Toast.makeText(getApplicationContext(), "REGISTER EMAIL SUCCESS", Toast.LENGTH_LONG).show();
    }

    @Override
    public void fail(String errorMessage) {
        String message = "REGISTER EMAIL ERROR ";
        if(errorMessage != null) {
            message = message + errorMessage;
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
};
EuroMobileManager.getInstance().registerEmail("test@euromsg.com", EmailPermit.ACTIVE, false, getApplicationContext(), callback);
```




## 5.Licences

 - [Related Digital ](https://www.relateddigital.com/)
 - [Euromessage](https://www.euromsg.com/)
