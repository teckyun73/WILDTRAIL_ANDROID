# Internal Test QA Package

Use this package for each Google Play internal testing candidate. It ties the signed build, screenshots, release notes, tester instructions, and known limitations into one repeatable handoff.

## Release Candidate Record

Fill this table before uploading the build. For CI-generated artifacts, set or confirm `VERSION_CODE` and `VERSION_NAME` with `scripts/set-release-version.ps1`, then copy the values from `.github/workflows/android-ci.yml`.

| Field | Value |
| --- | --- |
| Version code | `TBD` |
| Version name | `TBD` |
| Commit SHA | `TBD` |
| CI run URL | `TBD` |
| Backend environment | `staging` or `production` |
| API base URL owner | `TBD` |
| Maps API key state | configured / intentionally missing |
| Release signing | signed with upload key / unsigned verification only |
| Manual QA owner | `TBD` |
| Upload date | `YYYY-MM-DD` |

## Build And Upload Checklist

1. Run `scripts/rehearse-signed-release.ps1` when signing setup changed, then confirm `docs/release-signing.md` values are configured outside the repository.
2. Build the release candidate:

```powershell
.\gradlew.bat ktlintCheck testDebugUnitTest assembleRelease bundleRelease '-PVERSION_CODE=2' '-PVERSION_NAME=0.1.1' --stacktrace
```

3. Verify the release candidate artifacts and checksums:

```powershell
.\scripts\verify-release-candidate.ps1 -VersionCode 2 -VersionName 0.1.1 -RequireSigned
```

4. Confirm the signed bundle exists at `app/build/outputs/bundle/release/app-release.aab`.
5. Run `docs/manual-qa-checklist.md` on at least one emulator.
6. Run one physical-device smoke pass when a device is available.
7. Upload the signed AAB to Google Play internal testing.
8. Add release notes using the template below.
9. Send the tester instructions below with the internal testing opt-in link.

## Screenshot Capture Checklist

Capture screenshots from the same release candidate whenever possible. Use seeded or staging data that does not expose private API URLs, keys, personal location, or tester accounts.

| Screenshot | Required Content | Pass |
| --- | --- | --- |
| Identify entry | Image and audio identification actions visible. | `[ ]` |
| Identify result | Candidate species, confidence, and save action visible. | `[ ]` |
| Species guide | Search field and species list visible. | `[ ]` |
| Species detail | Description and hotspot recommendation visible. | `[ ]` |
| Records | At least one non-personal saved observation visible. | `[ ]` |
| Trip planner | Main planning inputs and generated plan visible. | `[ ]` |
| Native map | Route stops and map/setup state visible. | `[ ]` |

Save screenshot filenames with a version prefix, for example `0.1.1-01-identify-entry.png`.

## Release Notes Template

```text
WildTrail Android internal test {VERSION_NAME} ({VERSION_CODE})

What's included:
- Wildlife image and audio identification flows.
- Species guide with observation tips and hotspot recommendations.
- Observation records for saved identification results.
- Trip planning with native map view and external map handoff.

Please test:
- App launch and bottom navigation.
- API status check with the configured backend.
- Image identification and top-candidate save.
- Audio permission and recording flow.
- Records refresh after saving an observation.
- Trip generation, native map entry, marker/route details, and external map handoff.

Known limitations:
- Identification quality depends on the configured backend model or stub mode.
- Native map tiles require a valid Maps SDK API key.
- Production retention/deletion behavior must be confirmed before public launch.

Build metadata:
- Commit: {COMMIT_SHA}
- CI: {CI_RUN_URL}
- Backend: {BACKEND_ENVIRONMENT}
- APK SHA-256: {APK_SHA256}
- AAB SHA-256: {AAB_SHA256}
```

## Tester Message Template

```text
Hi, this is the WildTrail Android internal test build.

Install the app from the Google Play internal testing link, then try these flows:
1. Open the app and check each bottom tab.
2. In Status, run the backend health check.
3. In Identify, try image identification and save the top candidate.
4. In Records, confirm the saved observation appears.
5. In Trips, generate a plan and open the native map.
6. Try external map handoff from the trip/map screen.

Please report:
- Device model and Android version.
- What you were doing when the problem happened.
- Screenshot or screen recording if possible.
- Whether the issue repeats after closing and reopening the app.
```

## Known Limitations Checklist

Review this list before each internal test rollout and copy relevant items into release notes.

- [ ] Backend may run in model or stub mode depending on deployment state.
- [ ] Maps SDK key may be missing or restricted incorrectly in early builds.
- [ ] Audio recording quality varies by emulator/device microphone behavior.
- [ ] Observation deletion/editing is not yet exposed in the Android app.
- [ ] Privacy policy URL is still a draft until `docs/privacy-policy-draft.md` is finalized and published.
- [ ] Public production retention/deletion policy is not final until backend operations are confirmed.

## Post-Upload Monitoring

After rollout to internal testers:

1. Install through the Play internal testing link on at least one account.
2. Confirm the Play-delivered build version matches the release candidate record.
3. Check Play Console for crashes, ANRs, and pre-launch report results.
4. Collect tester feedback into issues using `.github/ISSUE_TEMPLATE/bug_report.yml`.
5. Decide whether to patch, promote, or retire the candidate.
