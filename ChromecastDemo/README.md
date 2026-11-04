# JW Player SDK for Android - Chromecast Demo

Casting from a `JWPlayerView` with the JW Player SDK's Chromecast module.

## What it shows

- Adding `com.jwplayer:jwplayer-chromecast` is what enables casting: the player shows a cast
  button in its controls once a Cast device is found on the network.
- `CastOptionsProvider` (registered in `AndroidManifest.xml` via
  `com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME`) configures the Cast
  framework — here the Default Media Receiver and the media notification actions.
- `CastContext.getSharedInstance(...)` is initialised only when Google Play services are
  available, since casting doesn't work without them.
- The app bar is hidden in fullscreen (unless a cast session is connected), rotating to landscape
  enters fullscreen, and Back exits fullscreen.

## SDK APIs

`jwplayer-chromecast` module, `JWPlayerView.getPlayer(LifecycleOwner)`, `PlayerConfig.Builder`,
`EventType.FULLSCREEN`, `JWPlayer.setFullscreen(...)`.

## Setup

1. Open the `ChromecastDemo` folder in Android Studio.
2. Set your JW Player license key as an environment variable before building; Gradle writes it
   into `BuildConfig.JWPLAYER_LICENSE_KEY`:

   ```
   export JWPLAYER_LICENSE_KEY=<your license key>
   ./gradlew :app:installDebug
   ```
3. Run on a device with Google Play services, on the same Wi-Fi network as a Cast device.

For more information, see the [JW Player Android developer guide](https://docs.jwplayer.com/players/docs/android-overview).
