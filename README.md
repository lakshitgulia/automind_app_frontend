# AutoMind App Frontend

AutoMind is an Android app built with Jetpack Compose for vehicle monitoring, predictive maintenance insights, alert handling, service scheduling flows, and locally stored user and vehicle management.

## Overview

The app currently includes:

- Local sign up, login, logout, and permanent account deletion
- Per-account vehicle storage using on-device persistence
- Home dashboard with live telemetry, prediction summaries, and AI insight cards
- Vehicle diagnostics screen with health, telemetry, and risk indicators
- Alerts screen with maintenance, service booking, reschedule, and cancel flows
- Profile screen for account details and vehicle management

## Current behavior

### Authentication and local storage

- User accounts are stored locally using `SharedPreferences`
- Login and signup are frontend/local only
- Account deletion removes:
  - the currently logged-in account
  - stored account data for that account
  - stored vehicle data for that account
- Vehicle data is stored per account bucket, not globally for all users

### Vehicle monitoring

- The app polls backend state repeatedly while the user is logged in and not on the login screen
- Polling starts only when the current account has at least one saved vehicle
- The active vehicle is selected from the locally saved primary vehicle
- Dashboard and diagnostics UI can display telemetry and prediction data such as:
  - speed
  - engine temperature
  - battery percentage
  - drive mode
  - range
  - gear / drive mode display
  - failure prediction summaries
  - health and service information

### Alerts and service flows

- The alerts screen shows warning, safety, info, and critical alert content
- Service flows support schedule, reschedule, and cancel behavior
- Service booking UI can show booking state like `BOOKED`
- Vehicle location labels use Android `Geocoder` when available and fall back to formatted coordinates

### UI state

- Profile shows a `VERIFIED` badge
- Profile vehicle cards focus on core identity and status information
- Home includes quick telemetry and AI insight sections

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit
- Moshi
- OkHttp
- Kotlin Coroutines

## Project structure

```text
app/src/main/java/com/automind/app/
├── data/
│   ├── local/
│   ├── model/
│   ├── network/
│   └── repository/
├── ui/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   │   ├── alerts/
│   │   ├── home/
│   │   ├── login/
│   │   ├── profile/
│   │   └── vehicle/
│   └── theme/
└── MainActivity.kt
```

## Important files

- `app/src/main/java/com/automind/app/MainActivity.kt`
  - app entry point, Retrofit setup, backend base URL, and polling lifecycle
- `app/src/main/java/com/automind/app/data/network/AutoMindApiService.kt`
  - backend API contract
- `app/src/main/java/com/automind/app/data/repository/VehicleRepository.kt`
  - backend response mapping, UI state management, alerts, and recommendations
- `app/src/main/java/com/automind/app/data/local/UserPreferences.kt`
  - local account storage, login flow, and delete-account support
- `app/src/main/java/com/automind/app/data/local/VehiclePreferences.kt`
  - per-account vehicle persistence and primary vehicle handling
- `app/src/main/java/com/automind/app/ui/navigation/NavGraph.kt`
  - app navigation graph and bottom navigation

## Backend

The app is currently configured to use:

`https://khushi1811-automind-rl.hf.space/`

Core endpoints used by the app include:

- `/state`
- `/reset`
- `/step`

Additional endpoints defined in the API interface include:

- `/`
- `/health`
- `/tasks`
- `/schema`

## Requirements

To build and run the project successfully, your local environment should have:

- Android Studio with Gradle sync support
- Android SDK Platform 34
- Build tools compatible with `compileSdk = 34`
- Java 17 / Gradle JDK 17-compatible setup

Project build settings currently include:

- `compileSdk = 34`
- `minSdk = 26`
- `targetSdk = 34`
- Kotlin JVM target `17`

## Running the app

1. Open the project in Android Studio.
2. Let Gradle sync finish using the included Gradle wrapper.
3. Make sure the project uses a Java 17-compatible Gradle JDK.
4. Ensure an emulator or physical device has working internet access, since live telemetry depends on the configured backend.
5. Run the `app` module.

## Notes

- Account management is frontend/local-storage based, not remote-auth based
- Live values depend on backend reachability and emulator/device network health
- Release signing is only configured when `keystore.properties` is present locally
- `local.properties`, `keystore.properties`, and keystore files are intentionally local-only
