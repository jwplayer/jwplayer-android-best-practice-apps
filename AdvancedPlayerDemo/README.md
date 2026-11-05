# Advanced Player

Shows how to drive the JW Player SDK from your own native UI, with VAST ads. It is the Android counterpart of the iOS `AdvancedPlayer` best practice app, and replaces the former `DemoNativeControls` sample.

## What it shows

- The SDK's built-in UI (including the ad UI) turned off.
- A custom control bar built from plain Android Views:
  - play / pause
  - seek bar with position and duration (hidden for live streams)
  - a playback-rate menu
  - a quality menu listing the stream's renditions
  - an **Ad** badge and a **Skip** button (with a countdown) during ads
- A VAST ad schedule with a pre-roll and a mid-roll.
- A JW Player playlist as content.

## How the pieces map to SDK APIs

| What | Where | SDK API |
|---|---|---|
| Create the player, tied to the Activity lifecycle | `AdvancedPlayerActivity` | `JWPlayerView.getPlayerAsync(context, lifecycleOwner, listener)` |
| Turn off the SDK UI | `AdvancedPlayerActivity.buildConfig` | `UiConfig.Builder().hideAllControls()` |
| Ad schedule | `AdvancedPlayerActivity.buildConfig` | `VastAdvertisingConfig.Builder().schedule(...)`, `AdBreak.Builder().tag(...).offset("pre" / "15")` |
| Content | `AdvancedPlayerActivity.buildConfig` | `PlayerConfig.Builder().playlistUrl(...)` |
| Play / pause | `PlayerControlsView` | `play()`, `pause()`, `getState()`; `PLAY`, `PAUSE`, `COMPLETE` events |
| Seek bar | `PlayerControlsView` | `seek(seconds)`; `TIME` event (content), `AD_TIME` event (ads) |
| Playback rate | `PlayerControlsView` | `setPlaybackRate(rate)`; `PLAYBACK_RATE_CHANGED` event |
| Quality | `PlayerControlsView` | `getQualityLevels()`, `setCurrentQuality(index)`; `LEVELS`, `LEVELS_CHANGED` events |
| Ad badge | `PlayerControlsView` | `AD_BREAK_START`, `AD_BREAK_END` events |
| Skip button | `PlayerControlsView` | `AdImpressionEvent.getSkipOffset()`, `AD_TIME`, `skipAd()`, `AD_SKIPPED` event |

Ad and content time arrive on different events, so the seek bar shows whichever is playing and is disabled during ads.

The sample VAST ad is not skippable by itself, so the config sets `VastAdvertisingConfig.Builder().skipOffset(5)` to show the Skip button. Remove it to respect each ad's own skip setting.

## Setup

The license key is read at build time from the `JWPLAYER_LICENSE_KEY` environment variable and exposed as `BuildConfig.JWPLAYER_LICENSE_KEY`:

```sh
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```

The ad tag is Google's public IMA sample VAST tag. Replace it and the playlist URL with your own.

For more information see the [Android developer guide](https://docs.jwplayer.com/players/docs/android-overview).
