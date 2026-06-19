# Android App Setup

[![Android CI](https://github.com/teckyun73/WILDTRAIL_ANDROID/actions/workflows/android-ci.yml/badge.svg)](https://github.com/teckyun73/WILDTRAIL_ANDROID/actions/workflows/android-ci.yml)

This folder contains the first native Android implementation of WildTrail.

## What Is Included

- Kotlin Android project
- Jetpack Compose app shell
- Bottom navigation with `식별`, `상태`, `도감`, `기록`, and `여행` tabs
- Retrofit API client
- `/health` backend connectivity check
- `GET /api/v1/species` species list integration
- `GET /api/v1/species/{species_id}` species detail integration
- `GET /api/v1/locations?species_id={species_id}` hotspot recommendations
- `POST /api/v1/identify/image` multipart image identification
- `POST /api/v1/identify/audio` multipart audio identification
- `POST /api/v1/sightings` observation record creation
- `GET /api/v1/sightings` observation record list
- Android Photo Picker image selection
- Local HTTP network security config for emulator development
- Trip planner with native Google Maps screen and external map app route handoff
- JVM unit tests for network utilities, API retry/fallback, ViewModels, route generation, and map helper logic

## Open In Android Studio

1. Open Android Studio.
2. Choose `Open`.
3. Select `C:\CURSUR_PJT\Wildtrail\android`.
4. Let Gradle Sync finish.
5. Run the `app` configuration on an emulator.

## Backend For Emulator

Start the existing FastAPI backend on the PC:

```powershell
cd "C:\CURSUR_PJT\ICT Education 20260429\backend"
.\.venv\Scripts\activate
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

In the Android emulator, use this API URL:

```text
http://10.0.2.2:8000
```

For a physical Android device on the same Wi-Fi network, use the PC LAN IP instead:

```text
http://<PC_LAN_IP>:8000
```

The status screen also includes API environment presets for build default, emulator local backend, ADB reverse, physical-device LAN, staging, and production. Configure build defaults in `local.properties` or Gradle properties:

```properties
API_BASE_URL=http://10.0.2.2:8000
DEBUG_API_BASE_URL=http://10.0.2.2:8000
RELEASE_API_BASE_URL=https://api.example.com
STAGING_API_BASE_URL=https://staging-api.example.com
PRODUCTION_API_BASE_URL=https://api.example.com
```

`DEBUG_API_BASE_URL` is used for debug builds, `RELEASE_API_BASE_URL` is used for release builds, and the staging/production values appear as selectable presets inside the app. If staging/production are not configured, they fall back to the debug/release defaults.

## Native Map Setup

The trip planner includes an in-app native map screen powered by Google Maps SDK for Android.

Enable `Maps SDK for Android` in Google Cloud Console, then create an API key with an Android app restriction for the debug build:

```text
Package name: com.wildtrail.app
SHA-1: E2:3E:F2:63:59:85:5F:2C:CE:2F:99:0A:AF:BC:14:3C:EB:78:AE:43
```

Set the Maps API key in `local.properties`:

```properties
MAPS_API_KEY=your_google_maps_sdk_key
```

When `MAPS_API_KEY` is empty, the app still opens the native map screen and route list safely, but map tiles and markers are replaced by an in-app setup notice.

After setting the key, run `.\gradlew.bat assembleDebug`, install the debug APK, generate a trip plan, and open `앱 지도 보기`. A successful setup shows Google map tiles, a marker on the wildlife hotspot, and the route stop list below the map.

## Local JDK

CI uses Temurin JDK 17 through GitHub Actions. On this Windows development PC, keep machine-specific JDK paths out of the repository and configure them in the user Gradle properties file instead:

```properties
# %USERPROFILE%\.gradle\gradle.properties
org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr
```

Do not commit `org.gradle.java.home` to the project `gradle.properties`; it breaks Linux CI runners.

## Current Screens

### 식별

- Uses Android Photo Picker to select an image
- Uploads the image to `POST /api/v1/identify/image`
- Shows backend message, source mode (`MODEL` or `STUB`), candidate species, and confidence
- Tapping a candidate opens the existing species detail and hotspot flow
- `상위 후보 기록 저장` posts the top candidate to `POST /api/v1/sightings`

### 상태

- API URL input with environment presets and build default reset
- `/health` check button
- Backend service status
- Image model mode: `MODEL` or `STUB`
- Audio model mode: `MODEL` or `STUB`
- LLM mode: `READY` or `OFF`
- Backend warnings

### 도감

- Loads `GET /api/v1/species`
- Local search by common name, scientific name, or species id
- Species rows with category, best months, and protection grade
- Tap a species to load `GET /api/v1/species/{species_id}`
- Selected species detail shows description, habitat, diet, breeding season, active time, observation tips, and similar species
- Selected species also loads related hotspots from `GET /api/v1/locations?species_id={species_id}`
- Hotspot panel shows top recommendations, region, score, access level, fee, best months, transport note, and safety note

### 기록

- Loads `GET /api/v1/sightings`
- Shows saved species name, timestamp, confidence, media type, location label, and note
- Refresh button reloads the backend list


## Release Build Preparation

The app version defaults are defined in `app/build.gradle.kts`:

```text
versionCode = 1
versionName = 0.1.0
```

Override them for CI or local release candidates with Gradle properties:

```powershell
.\gradlew.bat assembleRelease bundleRelease '-PVERSION_CODE=2' '-PVERSION_NAME=0.1.1'
```

Current release verification builds these artifacts:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
app/build/outputs/bundle/release/app-release.aab
```

The release APK is unsigned and intended for build verification only. Before Play Store or external distribution, create a private upload keystore outside the repository and wire signing through local/CI secrets.

## Repository Operations

Use the pull request and issue templates under `.github/` for scoped changes and bug reports. Release candidate and merge-gate guidance lives in [docs/release-operations.md](docs/release-operations.md), and hands-on emulator validation lives in [docs/manual-qa-checklist.md](docs/manual-qa-checklist.md).

## Verification

GitHub Actions runs this non-emulator verification on every `main` push and pull request:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease bundleRelease --stacktrace
```

Before handing off changes locally, also run the emulator UI smoke suite when an AVD is available:

```powershell
.\gradlew.bat connectedDebugAndroidTest --stacktrace
```

For a full local smoke check on the configured emulator, install and launch the debug APK after a successful build:

```powershell
$adb = "C:\Users\ATECCN\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apk = "C:\CURSUR_PJT\Wildtrail\android\app\build\outputs\apk\debug\app-debug.apk"
& $adb reverse tcp:8000 tcp:8000
& $adb install -r $apk
& $adb shell monkey -p com.wildtrail.app 1
& $adb logcat -d -t 300 | Select-String -Pattern "FATAL EXCEPTION|E AndroidRuntime|com\.wildtrail\.app.*FATAL"
```

No output from the final logcat filter means no recent fatal app crash was detected.

### Current Test Coverage

- Network URL normalization and user-facing error messages
- `ApiCallRunner` transient retry and emulator fallback behavior
- `StatusViewModel`, `SpeciesViewModel`, `TripsViewModel`, `RecordsViewModel`, and `IdentifyViewModel` state transitions
- Trip route stop generation and Google Maps / `geo:` URI construction
- Native map marker filtering, color mapping, and marker hue mapping
- Compose UI smoke checks for app startup, primary bottom navigation tabs, status URL editing and environment presets, records context, identify result save actions, species search/detail/hotspot handoff, and trip planning form editing

## Next Implementation Unit

The next practical step is to keep promoting the highest-value manual flows into stable UI tests:

1. Identify screen: exercise real picker/recording handoff around the already-covered result save action.
2. Species screen: add direct error-state retry checks around detail and hotspot loading.
3. Trips screen: generate a plan with a fake or seeded backend, open the native map, open route handoff.

Start with the manual QA checklist in [docs/manual-qa-checklist.md](docs/manual-qa-checklist.md), keep release/merge notes aligned with [docs/release-operations.md](docs/release-operations.md), then promote stable flows into Compose UI tests.

## Local Tooling Note

The terminal build on this PC uses the Android Studio bundled JBR configured in `%USERPROFILE%\.gradle\gradle.properties`. The Android SDK and emulator are expected under `C:\Users\ATECCN\AppData\Local\Android\Sdk` on this PC.

