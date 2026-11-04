# Fullscreen Without Rotation Demo

Shows a custom `FullscreenHandler` that makes the player fill the screen **without** rotating the
device to landscape.

## What it shows

- By default, entering fullscreen switches the Activity to landscape. Here,
  `JWPlayer.setFullscreenHandler(...)` replaces the default handler with
  `FullScreenHandler_NoRotation`, which only swaps the `JWPlayerView`'s layout params to
  `MATCH_PARENT` × `MATCH_PARENT` on enter and restores the original params on exit.
- The other `FullscreenHandler` callbacks (`onAllowRotationChanged`, `updateLayoutParams`, ...) are
  intentionally no-ops: this handler never changes orientation.
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
