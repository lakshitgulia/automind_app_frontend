# AutoMind App Frontend

AutoMind is an Android app built with Jetpack Compose for live vehicle monitoring, predictive alerts, service workflows, and account-based local personalization.

## What the app does

- Local sign up, login, logout, and permanent account deletion
- Vehicle management with per-account saved vehicles
- Live dashboard polling from the AutoMind backend
- Home screen for telemetry, predictions, and AI insight cards
- Vehicle diagnostics screen for deeper metrics and failure-risk data
- Alerts screen for smart alerts, maintenance status, and service scheduling
- Profile screen for account and vehicle management

## Current feature set

### Authentication and account flow

- Accounts are stored locally on-device using `SharedPreferences`
- Login and signup work fully on the frontend
- Delete Account permanently removes:
  - the currently logged-in account
  - local account credentials/profile data
  - that account's saved vehicle data
- After deletion, the user must create the account again to continue

### Vehicle monitoring

- Polls backend vehicle state on a repeating interval
- Displays:
  - speed
  - engine temperature
  - battery percentage
  - drive mode
  - range
  - health and prediction insights
- Gear display has been cleaned to show values like `D1` instead of `D1 Auto`

### Alerts and maintenance

- Shows critical, warning, and safety alerts
- Supports service request, reschedule, and cancel flows
- Displays service center, ETA, booking details, and resolved vehicle location name
- Scheduled state button now shows `BOOKED`

### UI updates included

- Cleaner telemetry cards with one-line values
- Removed extra symbols in AI insight timestamp text
- Simplified profile badge to only show `VERIFIED`
- Removed fuel level and distance blocks from the profile vehicle card
- Removed extra alert action labels like `LOCATE SERVICE` and `IGNORE`

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit
- Moshi
- Kotlin Coroutines
- OkHttp

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
│   └── screens/
└── MainActivity.kt
```

## Important files

- `app/src/main/java/com/automind/app/MainActivity.kt`
  - app entry point, Retrofit setup, polling lifecycle
- `app/src/main/java/com/automind/app/data/network/AutoMindApiService.kt`
  - backend API contract
- `app/src/main/java/com/automind/app/data/repository/VehicleRepository.kt`
  - maps backend payloads into UI state
- `app/src/main/java/com/automind/app/data/local/UserPreferences.kt`
  - local account storage, login flow, delete-account support
- `app/src/main/java/com/automind/app/data/local/VehiclePreferences.kt`
  - per-account vehicle persistence

## Backend

The app currently points to:

`https://automind-rl.onrender.com/`

Used endpoints include:

- `/state`
- `/reset`
- `/step`

## Running the app

1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Run the `app` module on an emulator or Android device.

## Notes

- The repo currently contains generated Android/Gradle artifacts in version control.
- Account management is frontend/local-storage based, not a remote auth system.
- Reverse geocoding for vehicle coordinates uses Android `Geocoder` and falls back to raw coordinates if resolution is unavailable.
