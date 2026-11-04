# IMA Ad Companions

Shows Google IMA companion ads next to the player with the JW Player SDK's IMA module.

## What it shows

- Two companion slots (300x250 and 728x90) are declared as `ImaCompanionSlot`s pointing at
  `FrameLayout`s in `activity_main.xml`, and passed to the ad config with
  `ImaVmapAdvertisingConfig.Builder().companionSlots(...)`. IMA renders matching companion creatives
  into those views while the ad plays.
- The ad tag is Google's public IMA sample VMAP pre-roll, requesting companions with
  `ciu_szs=300x250,728x90`.
- An `AD_COMPANIONS` listener logs the companion data (`AdCompanionsEvent.getCompanions()`) for
  apps that want to render companions themselves.

## SDK APIs

`jwplayer-ima` module, `ImaVmapAdvertisingConfig`, `ImaCompanionSlot`,
`EventType.AD_COMPANIONS` / `AdvertisingEvents.OnAdCompanionsListener`, `AdCompanion`.

## Requirements

Core library desugaring is enabled in `app/build.gradle` (`coreLibraryDesugaringEnabled true` and
the `desugar_jdk_libs` dependency): the Google IMA SDK requires it.

## Setup

Set your JW Player license key as an environment variable before building; Gradle writes it into
`BuildConfig.JWPLAYER_LICENSE_KEY`:

```
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```
