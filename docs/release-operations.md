# WildTrail Android Release Operations

This document is the lightweight operating guide for keeping `main` releasable.

## Branch And PR Flow

1. Create a short-lived branch from `main`.
2. Keep the change scoped to one feature, fix, or documentation update.
3. Open a pull request using `.github/pull_request_template.md`.
4. Wait for `Android CI` to pass.
5. Run emulator validation locally when the change touches UI, navigation, maps, media, network behavior, or crash risk.
6. Merge only after the PR checklist is complete or any skipped item is explained.

## Required Checks Before Merge

Run the non-emulator verification locally for app-code changes:

```powershell
.\gradlew.bat ktlintCheck testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease bundleRelease --stacktrace
```

Run the emulator suite when an AVD is available:

```powershell
.\gradlew.bat connectedDebugAndroidTest --stacktrace
```

Install and launch the debug APK for smoke validation:

```powershell
$adb = "C:\Users\ATECCN\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apk = "C:\CURSUR_PJT\Wildtrail\android\app\build\outputs\apk\debug\app-debug.apk"
& $adb reverse tcp:8000 tcp:8000
& $adb install -r $apk
& $adb shell monkey -p com.wildtrail.app 1
& $adb logcat -d -t 300 | Select-String -Pattern "FATAL EXCEPTION|E AndroidRuntime|com\.wildtrail\.app.*FATAL"
```

No output from the final `logcat` filter means no recent fatal app crash was detected.

## CI Artifacts

`Android CI` uploads these artifacts for every `main` push and pull request:

- `wildtrail-debug-apk`
- `wildtrail-debug-android-test-apk`
- `wildtrail-release-artifacts`
- `wildtrail-release-candidate-report`
- `wildtrail-jvm-test-report`

Artifacts are retained for 14 days. `wildtrail-release-candidate-report` contains the JSON output from `scripts/verify-release-candidate.ps1`, including version metadata and SHA-256 checksums. Treat the release APK as unsigned verification output unless signing is explicitly configured through private local or CI secrets. Release signing setup is documented in `docs/release-signing.md`.

## Release Candidate Checklist

1. Decide the release version.
2. Build with explicit version properties:

```powershell
.\gradlew.bat ktlintCheck testDebugUnitTest assembleRelease bundleRelease '-PVERSION_CODE=2' '-PVERSION_NAME=0.1.1' --stacktrace
```

3. Verify release candidate artifacts and checksums:

```powershell
.\scripts\verify-release-candidate.ps1 -VersionCode 2 -VersionName 0.1.1
```

For Play upload builds, require signed artifacts:

```powershell
.\scripts\verify-release-candidate.ps1 -VersionCode 2 -VersionName 0.1.1 -RequireSigned
```

4. Confirm these files exist:

```text
app/build/outputs/apk/release/app-release-unsigned.apk  # no signing values
app/build/outputs/apk/release/app-release.apk           # signing values present
app/build/outputs/bundle/release/app-release.aab
```

5. Run the manual QA checklist in `docs/manual-qa-checklist.md`.
6. Confirm API environment values and `MAPS_API_KEY` state are correct for the target build.
7. Confirm release signing values from `docs/release-signing.md` are present for Play upload builds.
8. Review `docs/privacy-and-permissions.md` and confirm Play Data safety answers still match the build.
9. Record the commit SHA, CI run URL, artifact names, checksums, and QA notes in the release notes.

## Current Quality Gates

- JVM unit tests cover network utilities, retry/fallback behavior, ViewModel state transitions, route generation, route summaries, and map helper logic.
- Compose UI tests cover app startup, primary navigation, status environment presets, seeded species/trips/map flows, and the common offline error panel.
- Manual QA covers media picking, audio recording, records save/list, backend health, species/hotspots, trips, native maps, external map handoff, and permission guidance.
- Release review includes app icon/splash resources, privacy and permission notes, release candidate artifact verification, and Play internal testing preparation in `docs/play-internal-testing.md`.

## Configuration Notes

Keep machine-specific values out of Git:

- `org.gradle.java.home` belongs in `%USERPROFILE%\.gradle\gradle.properties`.
- `MAPS_API_KEY` belongs in `local.properties` or CI secrets.
- Release signing values belong in `local.properties`, Gradle properties, environment variables, or CI secrets; see `docs/release-signing.md`.
- API URLs can be set with `API_BASE_URL`, `DEBUG_API_BASE_URL`, `RELEASE_API_BASE_URL`, `STAGING_API_BASE_URL`, and `PRODUCTION_API_BASE_URL`.
