# Google DAI Demo

Plays a Google IMA Dynamic Ad Insertion (DAI) stream with the JW Player SDK's IMA module.

## What it shows

- `ImaDaiSettings(videoId, cmsId, StreamType.HLS, null)` describes a video-on-demand DAI stream
  (Google's public `tears-of-steel` sample). For a live stream, use the
  `ImaDaiSettings(assetKey, streamType, apiKey)` constructor instead.
- The `PlaylistItem.file(...)` is the **fallback** stream: the player plays it automatically if the
  DAI stream request fails.
- An `AD_ERROR` listener logs DAI ad errors.
- Back exits fullscreen.

## SDK APIs

`jwplayer-ima` module, `ImaDaiSettings`, `PlaylistItem.Builder().imaDaiSettings(...)`,
`EventType.AD_ERROR`, `JWPlayerView.getPlayer(LifecycleOwner)`.

## Requirements

- Core library desugaring is enabled in `app/build.gradle` (`coreLibraryDesugaringEnabled true` and
  the `desugar_jdk_libs` dependency): the Google IMA SDK requires it.
- `minSdk` is 26 for this sample.

## Setup

Set your JW Player license key as an environment variable before building; Gradle writes it into
`BuildConfig.JWPLAYER_LICENSE_KEY`:

```
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```
