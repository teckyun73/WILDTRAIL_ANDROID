## Summary

- What changed:
- Why it changed:

## Verification

- [ ] `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease bundleRelease --stacktrace`
- [ ] `./gradlew connectedDebugAndroidTest --stacktrace` on an available emulator, or explain why it was not run
- [ ] Debug APK install and launch smoke check
- [ ] Recent `logcat` has no fatal crash lines

## App Flows Checked

- [ ] Status/API environment selection
- [ ] Identify image/audio/recording flow
- [ ] Species detail and hotspot flow
- [ ] Records list/save flow
- [ ] Trips planner and map flow
- [ ] Offline/network error recovery

## Release Impact

- [ ] No release impact
- [ ] Version/build artifact behavior changed
- [ ] Requires backend/API coordination
- [ ] Requires `MAPS_API_KEY` or environment configuration update

## Notes

Link related issues, CI runs, screenshots, or manual QA notes here.
