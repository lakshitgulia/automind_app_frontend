# AutoMind Android Frontend

AutoMind is an Android app built with Jetpack Compose for live vehicle telemetry, predictive maintenance insights, smart alerts, and service-booking flows powered by a remote backend.

## What This Repo Contains

This repository is the Android frontend only.

- Local user auth and account persistence
- Per-account vehicle storage on device
- Live dashboard screens backed by remote telemetry
- Predictive maintenance and alert rendering
- Service schedule, reschedule, and cancel flows

Khushi's backend lives in a separate repository and is not part of this codebase.

## Current Product Flow

After login, the app reads the locally selected primary vehicle and uses its license plate as the active backend `car_id`.

The frontend then:

1. fetches live backend state
2. renders backend-driven data across Home, Vehicle, and Alerts
3. polls while the user is on telemetry screens
4. stops polling in background to reduce load and ANR risk

## Main Screens

### Home

- greeting and connection status
- registered vehicle identity
- drive mode and range
- live system snapshot
- AI recommendation card
- quick telemetry cards

### Vehicle

- overall health score
- predictive maintenance summary
- telemetry and diagnostics
- failure-risk forecast

### Alerts

- backend alert grouping by severity
- maintenance planning
- service booking details
- edit and cancel booking flows

### Profile

- local account details
- saved vehicles
- primary vehicle selection
- logout and account deletion

## Backend Integration

The app is configured to use:

`https://khushi1811-automind-rl.hf.space/`

Primary endpoints used:

- `/state`
- `/reset`
- `/step`

The frontend maps backend payloads including:

- vehicle identity
- quick telemetry
- trip summary
- health summary
- safety summary
- maintenance and service booking data
- ML predictions
- active alerts
- observation and ECU data

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit
- Moshi with generated adapters
- OkHttp
- Kotlin Coroutines

## Project Structure

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

## Important Files

- `app/src/main/java/com/automind/app/MainActivity.kt`
  - backend wiring and polling lifecycle
- `app/src/main/java/com/automind/app/data/network/AutoMindApiService.kt`
  - Retrofit API contract
- `app/src/main/java/com/automind/app/data/repository/VehicleRepository.kt`
  - backend mapping, UI state, alerts, and service actions
- `app/src/main/java/com/automind/app/data/local/UserPreferences.kt`
  - local account persistence
- `app/src/main/java/com/automind/app/data/local/VehiclePreferences.kt`
  - local vehicle persistence and primary-vehicle selection
- `app/src/main/java/com/automind/app/ui/navigation/NavGraph.kt`
  - app navigation

## Build And Run

Requirements:

- Android Studio
- Java 17
- Android SDK 34
- internet access on emulator or device

Useful commands:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

## Recent Frontend Improvements

- moved screens to backend-driven values instead of fixed placeholders
- reduced UI load by throttling and serializing polling
- service actions now match live backend validation rules
- removed redundant or noisy UI elements
- improved Home and Alerts screen clarity

## Strong Next Enhancements

- Add a backend configuration layer with `BuildConfig` or flavors so the API URL is not hardcoded in `MainActivity`.
- Move manual dependency creation out of `MainActivity` into Hilt or a small DI container.
- Replace `SharedPreferences` auth and vehicle storage with Room or DataStore for stronger state handling.
- Add repository and mapper tests for backend payload parsing and service-action flows.
- Add loading, empty, and retry states per screen instead of relying mostly on snackbars.
- Add a small debug panel for current `car_id`, last fetch time, backend connection state, and last service action result.
- Introduce screenshots and a short demo section in this README for stronger portfolio presentation.

## Notes

- This repo is the frontend app only.
- Backend behavior depends on the live Hugging Face deployment.
- Release signing depends on local signing files and should stay local.
