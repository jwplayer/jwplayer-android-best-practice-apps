# BackgroundAudioDemo

Background audio with the **SDK's own media session** (JW Player SDK for Android 4.28.0 and later). The SDK runs the foreground service, the media notification and the lock-screen controls, and keeps them in sync with the player: title, description, artwork, play/pause, next/previous and seeking. This is the recommended way to add background audio.

If your app needs full control of the media session and notification instead, see [notifications-demo](../notifications-demo).

## What it shows

Pick a mode on the first screen, start playback, then press Home or lock the screen.

| Mode | Class | What it demonstrates |
|---|---|---|
| Default notification | `PlayerActivity` (no `ServiceMediaApi`) | `MediaServiceController.enable(activity, player, null)`: the SDK's default controls |
| Notification buttons | `LikeButtonsMediaApi` | `ServiceMediaApi#getNotificationButtons` with `NotificationButton`: a Like toggle (re-labelled with `invalidateNotificationButtons()`) and a Bookmark button, handled in `onNotificationButtonClick` |
| Legacy notification actions | `LegacyActionsMediaApi` | The pre-4.28.0 `addNotificationActions` / `getCompactActions` hooks, still supported: `getActionIntent` with `KEYCODE_MEDIA_REWIND` (seeks back 10 s) and `KEYCODE_MEDIA_PLAY_PAUSE` |

The **"Let the SDK handle audio focus"** checkbox on the first screen turns on the SDK's audio-focus handling (`ExoPlayerSettings#setAudioFocusHandlingEnabled`, SDK 4.28.0+, off by default) and shows the changes reported to `setAudioFocusListener`. The player screen also shows the reason for the last pause (`PauseEvent#getPauseReason`, e.g. `AUDIO_INTERRUPTION` when audio focus is lost). With it on, the player pauses when another app starts playing, pauses and resumes for short interruptions such as a call or an assistant prompt, and lets the system lower its volume for navigation prompts. Leave it off if your app manages audio focus itself. Independent of this setting, the player always pauses when headphones or a Bluetooth audio device disconnect.

## Setup

1. Permissions in `AndroidManifest.xml`: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` and, for Android 13+, `POST_NOTIFICATIONS` (also requested at runtime in `MainActivity`). The permission is recommended rather than required: media-session notifications are exempt from it, but your app's other notifications need it and some OEM builds hide media notifications without it. The SDK declares its `MediaService` itself.
2. `player.allowBackgroundAudio(true)`.
3. `MediaServiceController.enable(this, player, api)` in `onCreate`. It binds the service and unbinds it automatically when the activity is destroyed.

## Notes

- The system decides how many notification buttons it shows (usually 3 to 5, including play/pause). Put the important ones first.
- On Android 13+ the system media controls lay out buttons themselves and do not show disabled buttons.
- For legacy actions that are not media keys, use broadcast `PendingIntent`s: Android 13+ blocks an Activity intent started from the system media controls while the app is in the background. A manifest-declared receiver has no reference to the player, so it cannot control playback. To act on the player from a notification tap, use `getNotificationButtons` and `onNotificationButtonClick` (buttons mode).
