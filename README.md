# JW Player SDK for Android - Best Practice Apps

In this repository you will find several tech demos that use the JW Player SDK for Android

Refer to the README in each subdirectory for specific usage instructions.

## Sample Apps

- **ChromecastDemo** - A simple implementation of our Chromecast module. Allows you to cast to any available Chromecast devices
- **ChromecastFragmentDemo** - Similar to the ChromecastDemo app, this app uses the `JWPlayerSupportFragment` as the main video component
- **DemoNativeControls** - A barebones implementation of a native UI utilizing Android `View` elements
- **FullBackgroundAudio** - A demo of how you can setup the player to allow for background audio playback
- **LocalAssetPlayback** - Demonstrates the ability of the JW Player SDK to load locally stored assets. In the demo we utilize the built in Android **Assets** folder
- **RecyclerViewDemo** - Shows you how to use the `JWPlayerView` within a `RecyclerView`, including mutually exclusive playback of the videos
- **listview-fullscreen** - An older implementation on how you could use the `JWPlayerView` within a `ListView`
- **movable-player** - A fun tech demo that uses Android's new Additive Animation libraries to smoothly drag a `JWPlayerView` around the screen
- **conviva-integration** - An application with convivial analytics
- **notifications-demo** - An example of how you can setup a native Android `Notification` to control background audio playback
- **FullscreenWithoutRotationDemo** - An example of how you can setup `JWPlayerView` within a `FullscreenHandler` to control screen behavior avoiding the rotation

## Usage instructions

In general the following usage instructions apply to most sample projects:

- Clone this repository
- Start up Android Studio choose **Open an Existing Android Studio Project**
- Navigate to the location of the repository and select the root directory of the app you want to load.
- Click **OK**
- Update application to use your license key (use 1 option below).
  - set JWPLAYER_LICENSE_KEY as environment variable  
  - overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license in the application's main acitivity
  - change in app-level build.gradle
- Click **Run**

For more information about the JW Player SDK for Android, and how to use it, head over to our [Developer Portal](https://developer.jwplayer.com/jwplayer/docs/android-getting-started)
