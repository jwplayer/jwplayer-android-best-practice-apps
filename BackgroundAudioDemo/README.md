# BackgroundAudioDemo

Background audio with the **SDK's own media session** (JW Player SDK for Android 4.28.0 and later). The SDK runs the foreground service, the media notification and the lock-screen controls, and keeps them in sync with the player: title, description, artwork, play/pause, next/previous and seeking. This is the recommended way to add background audio.

If your app needs full control of the media session and notification instead, see [notifications-demo](../notifications-demo).

## What it shows

Pick a mode on the first screen, start playback, then press Home or lock the screen.

| Mode | Class | What it demonstrates |
|---|---|---|
| Default notification | `PlayerActivity` (no `ServiceMediaApi`) | `MediaServiceController.enable(activity, player, null)`: the SDK's default controls |
| Notification buttons | `LikeButtonsMediaApi` | `ServiceMediaApi#getNotificationButtons` with `NotificationButton`: a Like toggle (re-labelled with `invalidateNotificationButtons()`) and a Bookmark button, handled in `onNotificationButtonClick` |
| Legacy notification actions | `LegacyActionsMediaApi` | The pre-4.28.0 `addNotificationActions` / `getCompactActions` hooks, still supported: a broadcast action plus `getActionIntent` for play/pause |

## Setup

1. Permissions in `AndroidManifest.xml`: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` and, for Android 13+, `POST_NOTIFICATIONS` (also requested at runtime in `MainActivity`). The SDK declares its `MediaService` itself.
2. `player.allowBackgroundAudio(true)`.
3. `MediaServiceController.enable(this, player, api)` in `onCreate`. It binds the service and unbinds it automatically when the activity is destroyed.

## Notes

- The system decides how many notification buttons it shows (usually 3 to 5, including play/pause). Put the important ones first.
- On Android 13+ the system media controls lay out buttons themselves and do not show disabled buttons.
- For legacy actions, use broadcast `PendingIntent`s: Android 13+ blocks an Activity intent started from the system media controls while the app is in the background.
