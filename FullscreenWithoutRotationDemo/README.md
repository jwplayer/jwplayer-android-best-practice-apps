# Fullscreen Without Rotation Demo

Shows a custom `FullscreenHandler` that enlarges the player to fill its parent layout **without**
rotating the device to landscape. The status and navigation bars stay visible, so "fullscreen" here
means the Activity's content area, not the whole display.

## What it shows

- By default, entering fullscreen switches the Activity to landscape. Here,
  `JWPlayer.setFullscreenHandler(...)` replaces the default handler with
  `FullScreenHandler_NoRotation`, which only swaps the `JWPlayerView`'s layout params to
  `MATCH_PARENT` × `MATCH_PARENT` on enter and restores the original params on exit.
- The other `FullscreenHandler` callbacks (`onAllowRotationChanged`, `updateLayoutParams`, ...) are
  intentionally no-ops: this handler never changes orientation.
- The handler does not hide the system bars. To use the whole display, also hide them on enter (for
  example with `WindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars())`) and show
  them again on exit.
- Back exits fullscreen.

Use this as a starting point for any custom fullscreen behaviour, e.g. hiding other views, or
moving the player into another container (see the RecyclerView demo for that).

## SDK APIs

`FullscreenHandler`, `JWPlayer.setFullscreenHandler(FullscreenHandler)`,
`JWPlayer.setFullscreen(boolean, boolean)`, `JWPlayer.getFullscreen()`.

## Setup

Set your JW Player license key as an environment variable before building; Gradle writes it into
`BuildConfig.JWPLAYER_LICENSE_KEY`:

```
export JWPLAYER_LICENSE_KEY=<your license key>
./gradlew :app:installDebug
```
