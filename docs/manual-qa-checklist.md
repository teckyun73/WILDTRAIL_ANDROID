# WildTrail Android Manual QA Checklist

Use this checklist after `testDebugUnitTest` and `assembleDebug` pass. Run it on the configured Android emulator before handing off a feature build.

## Preflight

- [ ] FastAPI backend is running on the PC at port `8000`.
- [ ] Emulator can reach the backend through `http://10.0.2.2:8000` or `adb reverse tcp:8000 tcp:8000` plus `http://127.0.0.1:8000`.
- [ ] Debug APK installs successfully with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- [ ] App launches without a fatal crash in recent `logcat` output.
- [ ] If native map tiles are being verified, `MAPS_API_KEY` is set in `local.properties`.

## Status Flow

- [ ] Open the `상태` tab.
- [ ] Confirm the Base URL field shows the expected local API URL.
- [ ] Tap `상태 확인`.
- [ ] Confirm service status, image model, audio model, LLM state, and warnings render without layout overlap.
- [ ] Temporarily enter an invalid API URL and confirm a user-facing network error appears.
- [ ] Restore the working API URL and confirm health check recovers.

## Identify Flow

- [ ] Open the `식별` tab.
- [ ] Tap `이미지 고르기`, choose an image, and confirm `분석 중...` appears during upload.
- [ ] Confirm `식별 결과` shows backend message, source, candidates, confidence, and scientific name.
- [ ] Tap a candidate and confirm the species detail/hotspot flow opens.
- [ ] Tap `상위 후보 기록 저장` and confirm a success message appears.
- [ ] Tap `오디오 고르기`, choose an audio file, and confirm audio identification results render.
- [ ] Use `오디오 녹음`; record for more than one second, stop, and confirm the recording is uploaded and analyzed.
- [ ] Try stopping too quickly and confirm the short-recording guidance appears.

## Species Flow

- [ ] Open the `도감` tab.
- [ ] Confirm the species list loads.
- [ ] Type in `종 이름 검색` and confirm the list filters by common name, scientific name, or species id.
- [ ] Tap a species row.
- [ ] Confirm detail fields render: description, habitat, diet, breeding season, active time, observation tips, similar species.
- [ ] Confirm `추천 관찰지` shows hotspot name, region, score, fee, transport note, and safety note.
- [ ] Tap `이 종으로 여행 계획` and confirm the trip planner opens with the selected species.

## Records Flow

- [ ] Open the `기록` tab.
- [ ] Confirm saved observations load with species name, timestamp, confidence, media type, location, and note.
- [ ] Tap `새로고침` and confirm the list reloads without duplicate visual artifacts.
- [ ] Save a new candidate from the identify flow, return to `기록`, and confirm the new record appears.

## Trips And Map Flow

- [ ] Open the `여행` tab.
- [ ] Confirm `Species ID`, `출발지`, `일수`, `인원`, `월`, and `예산(원)` inputs are visible.
- [ ] Enter valid trip conditions and tap `여행 계획 생성`.
- [ ] Confirm the generated plan shows summary, total/per-person costs, day plan, checklist, and disclaimer.
- [ ] Tap `앱 지도 보기`.
- [ ] Confirm the `여행 지도` screen opens and the route stop list is visible.
- [ ] If `MAPS_API_KEY` is set, confirm map tiles, markers, and polyline render.
- [ ] If `MAPS_API_KEY` is empty, confirm the in-app setup notice appears instead of a crash.
- [ ] Tap a route stop and confirm the detail panel opens.
- [ ] Tap `장소 검색` and confirm an external map/search intent opens or a graceful error appears.
- [ ] Tap `경로 안내` and confirm external route handoff opens or a graceful error appears.
- [ ] Tap `내 위치`; grant permission and confirm the in-app guidance updates.
- [ ] Deny location permission and confirm the denial guidance appears without crashing.
- [ ] Tap `계획으로` and confirm returning to the trip plan.

## Regression Sweep

- [ ] Rotate the emulator or resize the window and confirm major screens keep readable layout.
- [ ] Navigate through all bottom tabs repeatedly and confirm state is preserved where expected.
- [ ] Relaunch the app and confirm startup health/species/records loading does not crash.
- [ ] Run the final smoke log check and confirm no fatal crash lines appear.

```powershell
$adb = "C:\Users\ATECCN\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb logcat -d -t 300 | Select-String -Pattern "FATAL EXCEPTION|E AndroidRuntime|com\.wildtrail\.app.*FATAL"
```
