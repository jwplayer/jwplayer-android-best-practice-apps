# JW Player SDK for Android - Local Asset Playback

This application contains an example implementation of loading local video assets into the JW Player SDK for Android.

## Usage instructions:

- Clone the repository into your Android Studio workspace: `git clone git@github.com:jwplayer/jwplayer-android-best-practice-apps.git`
- Open Android Studio and select: `Open an existing Android Studio project`
- Navigate to the `jwplayer-android-best-practice-apps` directory and select the `LocalAssetPlayback` folder
- Set your license key as the `JWPLAYER_LICENSE_KEY` environment variable before building
  (`export JWPLAYER_LICENSE_KEY=<your license key>`); Gradle writes it into
  `BuildConfig.JWPLAYER_LICENSE_KEY`. You can also hard-code it in the app-level `build.gradle`.

The demo application should now build and run. For more information on how to use our SDK refer to our developer guide:

[https://developer.jwplayer.com/sdk/android/docs/developer-guide/](https://developer.jwplayer.com/sdk/android/docs/developer-guide/)

## What it shows

This demo loads files from the Android Assets folder (`app/src/main/assets`) using
`file:///android_asset/...` URLs:

- `PlaylistItem.Builder().file(...)` — the local `.mp4` video
- `PlaylistItem.Builder().image(...)` — the local poster image
- `Caption.Builder().file(...)` — a local WebVTT captions track

## Things to note

This is an easy way to show support for local **Video Playback** along with support for local **Poster Images** and **Captions**.

When implementing this on your own, you may want to use the Android [Download Manager](https://developer.android.com/reference/android/app/DownloadManager) to download the video from the open web.

Accessing the local file system on Android will differ depending on which version of Android you are building against and your application's use case. You may allow users to download videos locally into a `temp` folder, or you may allow them to query their video library. To learn more about external file access on Android refer to the Android [Data Storage](https://developer.android.com/training/data-storage/files) documentation.

When downloading videos from the web, opt for `.mp4` conversions of the video asset. Streaming protocols like `HLS` or `DASH` have limited utility in an offline scenario and would require you to rewrite the manifest to account for the local segments.
