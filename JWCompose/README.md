# JWPlayer with Jetpack Compose Demo

Embeds a `JWPlayerView` in a Jetpack Compose UI, with a Compose control bar in place of the
player's built-in controls.

## What it shows

- **Hosting the view.** `Playback` wraps `JWPlayerView` in `AndroidView`. The view and its
  player are created inside `remember`, so recomposition never creates a new player.
- **Configuration changes.** `MainActivity` declares `configChanges` for rotation and resizing in
  the manifest, so the Activity — and the player — survive a rotation; Compose just recomposes
  with the new size.
- **Lifecycle.** The player comes from `JWPlayerView.getPlayer(lifecycleOwner)`, so it is released
  when the Activity is destroyed. A `DisposableEffect` attaches/detaches the ViewModel's listeners
  exactly once.
- **State.** `VideoViewModel` turns player events (`READY`, `PLAY`, `PAUSE`, `IDLE`, `ERROR`,
  `SETUP_ERROR`) into a `StateFlow<VideoState>` that the Compose `Controls` observe. The ViewModel
  never creates the player and drops its reference on detach, so it can't leak an Activity.
- The built-in controls are hidden with `UiConfig.Builder().hideAllControls()`.

## SDK APIs

`JWPlayerView`, `JWPlayerView.getPlayer(LifecycleOwner)`, `JWPlayer.setup(PlayerConfig)`,
`PlayerConfig.Builder().playlistUrl(...)`, `UiConfig`, `JWPlayer.addListeners/removeListeners`.

## Build setup

Kotlin 2.x with the Compose compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`) and the
Compose BOM; no `composeOptions`/`kotlinCompilerExtensionVersion` needed.

## Setup

Set your JW Player license key as an environment variable before building; Gradle writes it into
`BuildConfig.JWPLAYER_LICENSE_KEY`:

```
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```
