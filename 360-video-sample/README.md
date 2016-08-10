# JW Player 360 Sample

Contained in this directory is a sample app for the JW Player VR SDK.
Setting up this demo is a bit different from the other best practice apps.

This demo is designed to work with version 1.0.0 of the JW Player VR SDK and version 2.4.1 of the JW Player SDK for Android.
When using newer versions you may have to make minor changes to the code, as the sample apps are not necessarily in sync with the latest SDK release.

## Setting up the demo

1. Open the app/libs/ directory.
2. Copy the latest version of the JW Player Android SDK for Android to the app/libs/ directory, and rename it to: `jwplayer-android-sdk.aar`.
3. Copy the latest version of the JW Player VR SDK for Android to the app/libs/ directory and rename it to: `jwplayer-vr-sdk.aar`.
4. Copy base.aar and common.aar to the app/libs/ directory.
5. Open this project in Android Studio and enter your license key in the app's AndroidManifest.xml file.
6. You should now be able to build and run the project on your phone.
