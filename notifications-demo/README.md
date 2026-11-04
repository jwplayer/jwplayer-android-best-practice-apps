# notifications-demo

Background audio with an **app-owned media session and notification**. The app runs its own foreground service (`MediaPlaybackService`), `MediaSessionCompat` (`MediaSessionManager`) and `MediaStyle` notification (`NotificationWrapper`), and drives the JW Player from them.

Use this approach only if you need full control of the session or notification. Otherwise, let the SDK handle it: see [BackgroundAudioDemo](../BackgroundAudioDemo), which also shows how to add custom notification buttons.

## How it fits together

- `VideoActivity` binds `MediaPlaybackService` once playback starts (first frame) and unbinds in `onDestroy`, which stops the service and releases the session.
- The notification actions are `MediaButtonReceiver.buildMediaButtonPendingIntent` intents. androidx's `MediaButtonReceiver` (declared in the manifest) forwards them to `MediaPlaybackService`, which passes them to the session with `MediaButtonReceiver.handleIntent`; the session callback then drives the player.
- On Android 13+ the app requests `POST_NOTIFICATIONS`. A media-session notification is exempt from that permission, but requesting it is recommended for your app's other notifications and for OEM builds that hide media notifications without it.
