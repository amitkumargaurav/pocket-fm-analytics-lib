# Pocket FM Analytics Android Library

A lightweight analytics SDK for Android (minSdk 21) with:

- app registration by package name
- durable offline queue with SQLite
- dedupe-safe delivery through client-generated `event_id`
- anonymous identity continuity and later user linking through `identify`
- event APIs for impression, screen-time, and click with custom metadata

## Installation

```kotlin
dependencies {
  implementation("com.pocketfm:analytics:<version>")
}
```

## Quick start

```kotlin
PocketFmAnalytics.init(
    context = applicationContext,
    config = AnalyticsConfig(
        apiKey = "YOUR_API_KEY",
        registerUrl = "https://api.example.com/apps/register",
        ingestUrl = "https://api.example.com/events/ingest",
        flushIntervalMinutes = 15,
        maxBatchSize = 50,
        debugLoggingEnabled = false,
    ),
)

PocketFmAnalytics.trackImpression(
    screenName = "Home",
    itemName = "TopBanner",
    order = 1,
    orientation = "portrait",
    metadata = mapOf("campaign" to "summer-2026"),
)

PocketFmAnalytics.startScreen("EpisodeDetails")
// ... user browses ...
PocketFmAnalytics.endScreen("EpisodeDetails")

PocketFmAnalytics.trackClick(
    screenName = "EpisodeDetails",
    targetName = "PlayButton",
    metadata = mapOf("episode_id" to "ep-8831"),
)

PocketFmAnalytics.identify("user-123")
PocketFmAnalytics.flush()
```

## Logging behavior

Logs are private by default in production (`debugLoggingEnabled = false`).
Enable debug logs only in development.
