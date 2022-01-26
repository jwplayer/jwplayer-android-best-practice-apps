# VUDRM Widevine Demo

This demo showcases how you can setup VUDRM Widevine to enable online playback of DRM protected content with JW Player SDK for Android.

## Usage instructions:

- Clone the repository into your Android Studio workspace: `git clone git@github.com:jwplayer/jwplayer-android-best-practice-apps.git`
- Open Android Studio and select: `Open an existing Android Studio project`
- Navigate to the `jwplayer-android-best-practice-apps` directory and select the `VUDRMWidevineDemo` folder
- Open the `AndroidManifest.xml` file and replace `{YOUR_LICENSE_KEY}` with your license key
- Open the `JWPlayerViewExample.java` file and replace `{INSERT_CONTENT_URL}` with your content URL, replace `{INSERT_TITLE}` with your content title, and replace `{INSERT_TOKEN}` with your VUDRM token.
- Open the `VUDRMCallback.java` file and replace `{INSERT_CERTIFICATE_URL}` with your certificate URl


The demo application should now build and run. For more information on how to use our SDK refer to our developer guide:

[https://developer.jwplayer.com/sdk/android/docs/developer-guide/](https://developer.jwplayer.com/sdk/android/docs/developer-guide/)