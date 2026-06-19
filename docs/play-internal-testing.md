# Google Play Internal Testing Checklist

Use this checklist when preparing the first Google Play internal testing track for WildTrail Android.

## Build Inputs

- Choose a release version and increment `VERSION_CODE`.
- Configure `RELEASE_API_BASE_URL`, `PRODUCTION_API_BASE_URL`, and `MAPS_API_KEY` outside the repository.
- Configure release signing values using `docs/release-signing.md` before uploading to Play.
- Build the Android App Bundle with release properties:

```powershell
.\gradlew.bat bundleRelease '-PVERSION_CODE=2' '-PVERSION_NAME=0.1.1'
```

## Store Listing Draft

- App name: `WildTrail`.
- Short description: wildlife identification, observation records, and trip planning for field exploration.
- Confirm screenshots from the current app build for identify, species, records, trips, and native map screens.
- Use the privacy and permission notes in `docs/privacy-and-permissions.md` for the Data safety form.

## Internal Track Steps

1. Create or open the Google Play Console app entry for package `com.wildtrail.app`.
2. Complete app access, ads, content rating, target audience, data safety, and privacy-policy fields.
3. Upload the signed `app/build/outputs/bundle/release/app-release.aab`.
4. Add internal testers by email list or Google Group.
5. Add release notes with commit SHA, CI run URL, backend environment, and known limitations.
6. Roll out to internal testing only after manual QA passes on at least one emulator and one physical device when available.

## Post-Upload Smoke

- Install the Play-delivered build from the internal testing link.
- Check app launch, bottom navigation, API status, image identification, audio permission flow, record save/list, trip planning, native map setup, and external map handoff.
- Capture crash reports or ANRs from Play Console before widening tester access.
