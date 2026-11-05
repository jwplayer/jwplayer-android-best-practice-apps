# Custom Fragment UI

Shows how to host a `JWPlayerView` inside a `Fragment` and replace the built-in controls with
your own UI.

## What it shows

- `CustomPlayerFragment` owns the `JWPlayerView`. The player is created with
  `JWPlayerView.getPlayer(getViewLifecycleOwner())`, so it follows the fragment's view lifecycle.
- The built-in controls are hidden with `UiConfig.Builder().hideAllControls()`, and
  `CustomPlayerView` (play/pause, seek bar, fullscreen, ad progress, skip and "learn more"
  buttons) is added on top of the player.
- `CustomPlayerViewModel` turns player and ad events (`TIME`, `AD_TIME`, `AD_META`,
  `AD_BREAK_START/END`, ...) into `LiveData` the custom UI observes.
- `MainActivity` adds the fragment once (the FragmentManager restores it after recreation), swaps
  portrait/landscape constraints itself, lets Back exit fullscreen, and shows a short log of key
  player events below the player (`EventLog`).
- Content is a JW Player playlist with Google's public IMA sample VMAP tag (pre-, mid- and
  post-roll).

## SDK APIs

`JWPlayerView.getPlayer(LifecycleOwner)`, `PlayerConfig.Builder`, `UiConfig.Builder`,
`VmapAdvertisingConfig`, `JWPlayer.addListeners(...)`, `JWPlayer.setFullscreen(...)`,
`JWPlayer.skipAd()`, `JWPlayer.seek(...)`.

## Setup

Set your JW Player license key as an environment variable before building; Gradle writes it into
`BuildConfig.JWPLAYER_LICENSE_KEY`:

```
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```
