# JW Player SDK for Android - Best Practice Apps

In this repository you will find several tech demos that use the JW Player SDK for Android

Refer to the README in each subdirectory for specific usage instructions.

## Sample Apps

### Getting started
- **FullscreenWithoutRotationDemo** - A `JWPlayerView` with a custom `FullscreenHandler` that goes fullscreen without rotating the screen
- **LocalAssetPlayback** - Plays video, poster and captions bundled in the app's `assets` folder
- **JWCompose** - The player inside Jetpack Compose (`AndroidView`), kept across recomposition and configuration changes

### Player UI
- **AdvancedPlayerDemo** - Custom native controls (play/pause, seek, playback rate, quality, ad badge and skip) with the SDK UI turned off, plus a VAST pre-roll and mid-roll schedule
- **CustomUi** - Replacing one of the SDK's UI groups (Next Up) with your own Kotlin view, using the SDK's UI view models
- **CustomFragmentUi** - A custom player view hosted in a `Fragment`, with a small event log
- **StylingDemo** - Restyling the player's controls by overriding the SDK's resources (colors, dimensions, styles)

### Ads
- **ImaAdCompanions** - Google IMA ads with companion ad slots
- **GoogleDAIDemo** - Google IMA Dynamic Ad Insertion (DAI) for video on demand
- (VAST schedules are shown in **AdvancedPlayerDemo**)

### Playback features
- **CaptionsDemo** - Sideloaded and in-manifest captions, caption styling and position (`CaptionsConfig`, `CaptionPosition`) and a track picker
- **PipDemo** - Picture-in-picture: entering from a button or on Home, the PiP events and warnings, and pausing when the PiP window is closed
- **VerticalFeedDemo** - A full-screen vertical video feed (swipe up / down, one video per page) built on `ViewPager2` with a fixed pool of three players; the README explains the limits of running several players at once
- **RecyclerViewDemo** - Several players in a `RecyclerView`: one plays at a time, players are reused with the views, and fullscreen moves the player out of its cell
- **ChromecastDemo** - Casting with the JW Player Chromecast module

### Background audio
- **BackgroundAudioDemo** - Background audio with the SDK's own media session, notification and lock-screen controls, custom notification buttons and the optional SDK audio-focus handling (SDK 4.28.0+). The recommended way to add background audio
- **notifications-demo** - Background audio with an app-owned media session and `Notification`, for apps that need full control of them

### DRM and offline
- **DrmDemo** - Online Widevine DRM with a licence URL (`DrmConfig`), or a custom `MediaDrmCallback`
- **OfflineDownloadsDemo** - Offline downloads, including DRM, managed by the SDK (`OfflineDownloadManager`). The recommended way to add offline playback
- **OfflineDelegateDemo** - Offline downloads, including DRM, through the app's own Media3 download service and the SDK's `OfflineDelegate` hook

## Usage instructions

In general the following usage instructions apply to most sample projects:

- Clone this repository
- Start up Android Studio choose **Open an Existing Android Studio Project**
- Navigate to the location of the repository and select the root directory of the app you want to load.
- Click **OK**
- Update application to use your license key (use 1 option below).
  - set JWPLAYER_LICENSE_KEY as environment variable  
  - overwrite BuildConfig.JWPLAYER_LICENSE_KEY with your license in the application's main activity
  - change in app-level build.gradle
- Click **Run**

For more information about the JW Player SDK for Android, and how to use it, head over to our [Developer Portal](https://docs.jwplayer.com/players/docs/android-overview)

## Building all samples

`./buildAll.sh` builds every sample (each folder with its own Gradle wrapper). The same builds run in GitHub Actions on every pull request.
