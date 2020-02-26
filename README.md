
<p align="center">
  <a target="_blank" rel="noopener noreferrer" href="https://github.com/relateddigital/euromessage-android"><img src="https://github.com/relateddigital/euromessage-android/blob/heads/feature/carousel_implementation/app/euromessage.png" alt="Euromessage Android Library" width="600" style="max-width:100%;"></a>
</p>


# Latest Version 

***February 20, 2020*** - [Euromessage v3.0.1](https://github.com/relateddigital/euromessage-android/releases/tag/3.0.1) 


# Table of Contents

- [Latest Version](#latest-version)
- [Euromessage Android](#euromessage-android)
  * [Installation](#installation)
      - [Note :](#note--)
    + [Permission](#permission)
    + [Sample Applications](#sample-applications)
    + [Using the SDK](#using-the-sdk)
      - [Initialization](#initialization)
      - [Sync](#sync)
    + [Licence](#licence)


# Euromessage Android

The Euromessage Android Sdk is a java implementation of an Android client for Euromessage.

## Installation

Add Euromessage to the ```dependencies``` in app/build.gradle.

```java
implementation 'com.euromsg:euromsg:3.0.1' 
```
 
#### Note : 

You may use [Euromessage Sdk](https://github.com/relateddigital/euromessage-android/tree/master/euromsg) directly.
  
  For that :
- Download the module
- Open your project which you want to use Euromessage
- Follow steps : Android Studio -> File -> New -> Import Module and select path where you want to locate module and rename it.


### Permission
```xml
     <uses-permission android:name="android.permission.INTERNET"/>

     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> 
 ```    
    
### Sample Applications 

- [Euromessage Sample Application](https://github.com/relateddigital/euromessage-android/releases/tag/3.0.1) 
 (master branch)

- [Euromessage Sample Application - (Support Library) ](https://github.com/relateddigital/euromessage-android/tree/euromessage-support)

### Using the SDK
 
 #### Initialization
```java

  public static String ENTEGRASYON_ID = Constant.ENTEGRASYON_ID;  // e.g.: "euromessage-android"

  EuroMobileManager euroMobileManager = EuroMobileManager.sharedManager(ENTEGRASYON_ID, this);

   euroMobileManager.registerToFCM(getBaseContext()); 
   ```
  
 #### Sync
 
 RMC needs that at least one property which is email or user id in order to match and analyse users.
 
 ```java
 
    euroMobileManager.setEmail("test@mail.com", this);
    euroMobileManager.setEuroUserId("12345", this);

    euroMobileManager.sync(this);
```
    
RMC Campaign and more information :  [Please check docs](https://docs.relateddigital.com/display/KB/Android+SDK). 

### Licence


 [Related Digital ](https://www.relateddigital.com/)
