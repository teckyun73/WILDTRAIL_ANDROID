# Privacy And Permission Notice

WildTrail is a field-guide and trip-planning app for wildlife observation. This notice documents the data and permissions currently used by the Android app so release candidates can be reviewed consistently before Google Play internal testing.

## Current Data Use

The app sends the following user-initiated data to the configured WildTrail backend API:

- Selected image files when the user requests image identification.
- Recorded audio files when the user requests audio identification.
- Observation records when the user taps the save action for an identification result.
- Trip planning inputs such as selected species, region, day count, daily hours, and mobility mode.

The app also reads species, hotspot, sighting, health, and trip-plan responses from the backend. API URL values can be changed inside the app status screen or configured through Gradle properties for each build environment.

## Android Permissions

| Permission | Why It Is Requested | User-Facing Trigger |
| --- | --- | --- |
| `INTERNET` | Connect to the WildTrail backend and Google Maps services. | App startup, backend checks, species, identification, records, trips, and maps. |
| `ACCESS_NETWORK_STATE` | Detect network availability for clearer offline/error messages. | Network-dependent screens. |
| `RECORD_AUDIO` | Capture wildlife audio when the user starts audio identification. | Identify screen recording action. |
| `ACCESS_FINE_LOCATION` | Show current-location context on native map flows when granted. | Native trip map location action. |
| `ACCESS_COARSE_LOCATION` | Provide approximate location fallback for native map flows when granted. | Native trip map location action. |

## Storage And Media

Image selection uses the Android Photo Picker where available. The app does not require broad external-storage permissions. Temporary media selected or recorded for identification should be treated as user-provided content and sent only after an explicit user action.

## Maps

Native maps use Google Maps SDK for Android. A missing or invalid `MAPS_API_KEY` should show an in-app setup notice instead of crashing. Route handoff to external map apps is user-initiated from trip screens.

## Release Review Checklist

Before an internal test build:

1. Confirm production API URL values are correct for the intended backend.
2. Confirm `MAPS_API_KEY` is configured through private local properties or CI secrets.
3. Verify the identify screen clearly communicates image/audio upload actions.
4. Verify location permission prompts appear only inside map/location flows.
5. Update the Google Play Data safety form to match the data types and purposes above.
6. Attach or link this notice from release notes when the permission set changes.
