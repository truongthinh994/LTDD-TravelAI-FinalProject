# TravelAI

TravelAI is an Android travel-planning application built with Kotlin and
Jetpack Compose. It combines AI-assisted itinerary planning with trip history,
budget and checklist management, weather forecasts, maps, landmark scanning,
PDF export, and trip sharing.

## Main Features

- Chat with DeepSeek AI to create travel plans.
- Review itineraries, budgets, smart checklists, and saved trips.
- View weather forecasts and trip locations on Google Maps.
- Scan landmarks with a vision provider and review scan history.
- Export a trip as a PDF or share trip details from Android.

## Technology Stack

- Kotlin, Jetpack Compose, and Material 3
- Hilt dependency injection
- Room database with schema migrations
- Retrofit and OkHttp
- DataStore preferences
- Google Maps Compose
- JUnit unit tests

## Team Contributions

The Week 3 collaboration repository uses three feature branches. Each teammate
contributed 15 application commits before the branches were merged into
`main`.

| Team member | GitHub account | Primary ownership | Application commits |
| --- | --- | --- | ---: |
| Truong Thinh | `truongthinh994` | Core UI: chat, planner, itinerary, and history | 15 |
| Nguyen Huu Nghia | `nguyenhuunghia10t1-creator` | Data layer: models, Room, APIs, repositories, and DI | 15 |
| Doan Anh Khoa | `AnhKhoaDoan` | Secondary features: map, weather, landmark scanner, profile, settings, and sharing | 15 |

See [Git workflow](docs/GIT_WORKFLOW.md) after the workflow documentation PR is
merged for the branch model, review rules, and conflict-handling process.

## Local Setup

1. Open the project in Android Studio.
2. Use the Android Studio JBR or JDK 17.
3. Create `local.properties` in the repository root.
4. Add your local Android SDK path and the API keys required for the features
   you want to test.
5. Run `.\gradlew.bat testDebugUnitTest` and then launch the `app`
   configuration.

Use placeholders when documenting configuration. Never commit real API keys,
keystores, or passwords.

```properties
sdk.dir=C\:\\path\\to\\Android\\Sdk
DEEPSEEK_API_KEY=<your-deepseek-api-key>
MAPS_API_KEY=<your-google-maps-api-key>
GEMINI_API_KEY=<your-gemini-api-key>
OPENCODE_API_KEY=<your-opencode-api-key>
```

`local.properties` is ignored by Git. Weather forecasts use Open-Meteo and do
not require an API key.

## Useful Commands

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

## Repository Hygiene

- Keep generated folders such as `build/` out of Git.
- Keep `main` stable and merge changes through reviewed pull requests.
- Use feature branches and Conventional Commit messages.
