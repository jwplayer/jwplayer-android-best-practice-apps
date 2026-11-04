# CaptionsDemo

Captions with the JW Player SDK for Android: sideloaded tracks, subtitles from an HLS manifest, styling and placement, and a custom track picker.

## What it shows

| Feature | How |
|---|---|
| Sideloaded caption tracks | `PlaylistItem.Builder#tracks` with `Caption.Builder` (`file`, `kind(CaptionType.CAPTIONS)`, `label`, `isDefault`). Item 1 has one WebVTT track that is on at start; item 2 has English and Spanish tracks (SRT files, which the SDK also accepts) |
| In-manifest subtitles | Item 3 is an HLS stream that declares its subtitles in the manifest. No `tracks` needed |
| Styling | `PlayerConfig.Builder#captionsConfig` with `CaptionsConfig.Builder`: `fontSize` (sp), `color`, `backgroundColor`, `edgeStyle` |
| Placement | `CaptionsConfig.Builder#position` with `CaptionPosition.Builder`: `width`, `alignment`, `linePercentage`. Applies to cues that carry no position of their own |
| Track picker | A `Spinner` filled from `EventType.CAPTIONS_LIST` and kept in sync with `EventType.CAPTIONS_CHANGED`; selecting an entry calls `player.setCurrentCaptions(index)` |

On Android the captions list always starts with an "Off" entry: index 0 turns captions off and the tracks start at index 1. (iOS uses -1 for off, so if you share logic across platforms, select by label.)

## System caption settings

The viewer's caption preferences in **Settings > Accessibility > Captions** (or Caption preferences) interact with `CaptionsConfig`:

- `CaptionsConfig.OVERRIDE_STRATEGY_VIDEO_OVERRIDE_ONLY` (default, used here): the system choice wins for every property the viewer set; your values fill in the rest. On a device with no caption preferences, all your values apply.
- `CaptionsConfig.OVERRIDE_STRATEGY_ALWAYS`: your values always win, over both system settings and styling embedded in the track. Use with care, it overrides deliberate accessibility choices.
- `allowScaling(false)` stops the system caption size scale from being applied on top of `fontSize`.

## Setup

Set your license key as the `JWPLAYER_LICENSE_KEY` environment variable (or edit `app/build.gradle`), then build and run the `app` module. Requires JW Player SDK 4.28.0.

## Content

- `https://cdn.jwplayer.com/manifests/NFl4Yrhl.m3u8` with `https://cdn.jwplayer.com/tracks/EcLZ5shp.vtt`
- `https://cdn.jwplayer.com/manifests/QUxmKbSS.m3u8` with `https://cdn.jwplayer.com/tracks/a2J6cY6H.srt` (English) and `https://cdn.jwplayer.com/tracks/0gyZJgjM.srt` (Spanish)
- Apple's advanced HLS example, `https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8`, for in-manifest subtitles
