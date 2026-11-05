# PipDemo

Picture-in-picture (PiP) with the JW Player SDK for Android.

## What it shows

| Feature | How |
|---|---|
| Opt the activity in to PiP | `android:supportsPictureInPicture="true"`, `resizeableActivity="true"` and `configChanges` (so the activity, and the player, are not recreated when the window resizes) in `AndroidManifest.xml` |
| Register with the SDK | `player.registerActivityForPip(activity, getSupportActionBar())` in `onCreate`, `deregisterActivityForPip()` in `onDestroy` |
| PiP button | `player.enterPictureInPictureMode()` |
| Auto-enter on Home | `player.setPipOnBackground(true)` (the default). The SDK handles the Home press itself, using the system's auto-enter on Android 12+, so the app does not override `onUserLeaveHint()` |
| Hide app UI in PiP | `onPictureInPictureModeChanged` hides everything except the player and restores it on exit. The SDK hides its own controls (and the action bar you passed in) |
| Pause when the PiP window is closed | Done by the SDK, as long as the activity forwards `onPictureInPictureModeChanged` to `player.onPictureInPictureModeChanged(...)` |
| PiP events and warnings | `EventType.PIP_OPEN`, `EventType.PIP_CLOSE` and `EventType.WARNING` (codes `ErrorCodes.ERROR_PIP_*`, e.g. PiP unsupported or disabled for the app) shown in the status line and logged |

Content: the playlist `https://cdn.jwplayer.com/v2/playlists/9L8aDjaU`.

## Setup

Set your license key as the `JWPLAYER_LICENSE_KEY` environment variable (or edit `app/build.gradle`), then build and run the `app` module. Requires JW Player SDK 4.28.0.

## Device notes

- PiP needs Android 8.0 (API 26) or later. On older versions, and on devices without PiP (some Android Go and TV devices), the SDK reports a PiP warning instead of entering PiP.
- Users can turn PiP off per app (Settings > Apps > Special app access > Picture-in-picture). The SDK reports that as a warning too.
- Auto-enter on Home works on stock Android but some OEM builds restrict or ignore it. The PiP button works on those devices.
- Use `launchMode="singleTask"` (as here) so that expanding the PiP window returns to the same activity.
