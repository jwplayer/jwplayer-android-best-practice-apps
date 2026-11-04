# notifications-demo

Background audio with an **app-owned media session and notification**. The app runs its own foreground service (`MediaPlaybackService`), `MediaSessionCompat` (`MediaSessionManager`) and `MediaStyle` notification (`NotificationWrapper`), and drives the JW Player from them.

Use this approach only if you need full control of the session or notification. Otherwise, let the SDK handle it: see [BackgroundAudioDemo](../BackgroundAudioDemo), which also shows how to add custom notification buttons.
