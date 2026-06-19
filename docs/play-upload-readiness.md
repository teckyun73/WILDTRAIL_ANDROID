# Play Upload Readiness Gate

Use this gate before uploading a WildTrail Android release candidate to Google Play internal testing. This document focuses on external-console setup and human decisions that cannot be fully verified by local builds or CI.

## Gate Summary

Do not upload the AAB until every required item below has an owner and status.

| Area | Required Before Upload | Owner | Status |
| --- | --- | --- | --- |
| Google Play app entry | Package `com.wildtrail.app` exists in Play Console. | `TBD` | `[ ]` |
| Release artifact | Signed `app-release.aab` is built and verified with `-RequireSigned`. | `TBD` | `[ ]` |
| Version metadata | `VERSION_CODE` and `VERSION_NAME` match the intended RC. | `TBD` | `[ ]` |
| Upload key | Real upload key is backed up outside the repository. | `TBD` | `[ ]` |
| Backend | Staging or production backend URL is selected and reachable. | `TBD` | `[ ]` |
| Maps | Maps SDK key is configured and Android app restriction is correct. | `TBD` | `[ ]` |
| Privacy policy | Final policy is published at a stable HTTPS URL. | `TBD` | `[ ]` |
| Data safety | Play Data safety answers match app behavior and policy. | `TBD` | `[ ]` |
| Store listing | App copy, screenshots, and graphic assets are ready. | `TBD` | `[ ]` |
| Testers | Internal tester email list or Google Group is ready. | `TBD` | `[ ]` |
| QA | Manual QA checklist passed on emulator and physical device when available. | `TBD` | `[ ]` |
| Release notes | Internal test release notes include commit, CI URL, backend, and checksums. | `TBD` | `[ ]` |

## Local And CI Evidence

Collect these values before upload:

```text
Version code:
Version name:
Commit SHA:
CI run URL:
RC report artifact: wildtrail-release-candidate-report
AAB path: app/build/outputs/bundle/release/app-release.aab
AAB SHA-256:
APK SHA-256:
Backend environment:
Maps API key state:
Manual QA owner:
```

Required commands:

```powershell
.\scripts\set-release-version.ps1 -VersionCode 2 -VersionName 0.1.1
.\scripts\rehearse-signed-release.ps1 -VersionCode 2 -VersionName 0.1.1-rehearsal
.\gradlew.bat ktlintCheck testDebugUnitTest assembleRelease bundleRelease '-PVERSION_CODE=2' '-PVERSION_NAME=0.1.1' --stacktrace
.\scripts\verify-release-candidate.ps1 -VersionCode 2 -VersionName 0.1.1 -RequireSigned
```

## Play Console Fields

Complete these Play Console sections before rollout:

- App access: document whether testers need credentials or special backend access.
- Ads: confirm the app does not show ads unless that changes.
- Content rating: complete the questionnaire using current app behavior.
- Target audience: choose the intended tester/audience profile.
- Data safety: align answers with `docs/privacy-and-permissions.md` and the final privacy policy.
- Privacy policy: provide the final HTTPS URL.
- Internal testing: add tester list or Google Group and upload signed AAB.
- Release notes: use `docs/internal-test-qa-package.md`.

## Go / No-Go Questions

Answer these before pressing rollout:

1. Is the backend environment stable enough for testers during the test window?
2. Are image/audio uploads expected and clearly described to testers?
3. Are location prompts limited to native map/location flows?
4. Does the app behave gracefully when Maps configuration fails?
5. Is the privacy policy URL final enough for internal testing?
6. Are there known limitations that testers must see in release notes?
7. Is there a feedback path for crashes, screenshots, and device details?

## No-Go Conditions

Do not roll out when any item below is true:

- Signed AAB verification fails.
- Upload key location or password ownership is unclear.
- Production/staging backend URL is unknown or unreachable.
- Maps SDK key is missing when map screenshots or map testing are required.
- Privacy policy contact, retention, or deletion details are still blank for a wider test.
- Data safety answers are not reviewed against current app behavior.
- Manual QA found a crash in app launch, navigation, identification, records, trips, or native maps.

## Post-Rollout Owner Tasks

After internal rollout:

1. Install from the Play internal testing link with a tester account.
2. Confirm app version matches the readiness record.
3. Check Play Console pre-launch report, crashes, and ANRs.
4. File tester feedback using the GitHub issue templates.
5. Decide whether the candidate is patched, promoted, or retired.
