# Release Signing

WildTrail release signing is optional by default so CI can continue to build unsigned verification artifacts. When all signing values are present, Gradle signs the release APK and AAB automatically.

## Private Values

Never commit upload keystores or passwords. The project reads these values from `local.properties`, Gradle properties, or environment variables:

```properties
RELEASE_STORE_FILE=C:/secure/wildtrail-upload.p12
RELEASE_STORE_PASSWORD=replace-with-private-password
RELEASE_KEY_ALIAS=wildtrail-upload
RELEASE_KEY_PASSWORD=replace-with-private-password
```

`RELEASE_STORE_FILE` can be an absolute path or a path relative to the Android project root. Keep the keystore outside the repository when possible. Repository ignore rules also exclude `*.jks`, `*.keystore`, `*.p12`, and `keystores/` as a backstop.

## Create A Local Upload Key

Use Android Studio's bundled JDK or another JDK 17+ installation:

```powershell
$keytool = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
& $keytool -genkeypair `
    -v `
    -keystore "C:\secure\wildtrail-upload.p12" `
    -storetype PKCS12 `
    -keyalg RSA `
    -keysize 2048 `
    -validity 10000 `
    -alias wildtrail-upload
```

Back up the keystore and passwords in the team's password manager before uploading an internal test build. If Google Play App Signing is enabled, this is the upload key, not the app signing key that Google uses for store delivery.

## Signed Rehearsal Without The Upload Key

Before using the private upload key, run a signed release rehearsal with a temporary PKCS12 key. The script creates a short-lived key in the temp directory, builds signed release artifacts, runs `scripts/verify-release-candidate.ps1 -RequireSigned`, and deletes the temporary key in a cleanup step.

```powershell
.\scripts\rehearse-signed-release.ps1 -VersionCode 2 -VersionName 0.1.1
```

This rehearsal proves that Gradle signing configuration, APK/AAB generation, and signed artifact verification are wired correctly. It does not produce Play-uploadable artifacts because the temporary key is not the real upload key.

## Build Signed Release Artifacts Locally

After adding the private values to `local.properties`, build with explicit version properties:

```powershell
.\gradlew.bat clean bundleRelease assembleRelease '-PVERSION_CODE=2' '-PVERSION_NAME=0.1.1'
```

Expected outputs:

```text
app/build/outputs/bundle/release/app-release.aab
app/build/outputs/apk/release/app-release.apk
```

If signing values are missing, the AAB/APK still build for verification but should be treated as unsigned and not uploaded to Play.

## CI Secret Names

For a future signed CI release job, store the same values as GitHub Actions secrets:

- `RELEASE_STORE_FILE`: path to the decoded keystore on the runner, or set this after decoding a base64 secret.
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

If the keystore is stored as base64, decode it in the workflow into a temporary file and set `RELEASE_STORE_FILE` to that file path. Do not upload signed release artifacts from pull requests from forks.

## Verification

Before uploading to Play internal testing:

1. Run `./gradlew ktlintCheck testDebugUnitTest assembleRelease bundleRelease --stacktrace`.
2. Confirm `app-release.aab` was produced with the intended `VERSION_CODE` and `VERSION_NAME`.
3. Upload only through Google Play Console internal testing unless a separate distribution process has been approved.
