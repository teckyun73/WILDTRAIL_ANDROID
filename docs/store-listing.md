# Google Play Store Listing Draft

This document keeps the first WildTrail Android store-listing materials in one place. Treat the copy below as a release-candidate draft and revise it after screenshots and final backend availability are confirmed.

## App Identity

- App name: `WildTrail`
- Package name: `com.wildtrail.app`
- Category candidate: travel, local exploration, or education depending on the final store positioning.
- Primary audience candidate: wildlife watchers, field learners, educators, and nature-trip planners.

## Short Description

Wildlife identification, observation records, and trip planning for field exploration.

## Full Description Draft

WildTrail helps nature explorers identify wildlife, keep observation records, and plan field trips around species and recommended hotspots.

Use image or audio identification to check a possible species, review candidate results, and save the strongest match as an observation record. Browse the species guide to learn habitat, diet, breeding season, active time, observation tips, and related hotspots. Plan wildlife trips by species, region, travel time, and mobility mode, then open the route in the in-app native map or hand it off to an external map app.

Key features:

- Image and audio identification through the configured WildTrail backend.
- Species guide with searchable species profiles and observation tips.
- Observation records for saved identification results.
- Trip planner with species, region, days, daily hours, and mobility options.
- Native Google Maps screen for route context and wildlife hotspots.
- External map handoff for route navigation.
- Environment presets for local, staging, and production API testing.

WildTrail is intended for responsible wildlife observation and trip preparation. Identification results can be uncertain and should be treated as field-support information, not as an official biological survey or safety guarantee.

## What's New Template

Initial internal testing build for WildTrail Android.

- Adds wildlife image and audio identification flows.
- Adds species guide, observation records, and trip planning.
- Adds native map view and external map handoff for planned routes.
- Adds release signing, privacy/permission review notes, and internal testing checklist.

## Screenshot Plan

Capture screenshots from the same signed release candidate when possible. Avoid showing private API URLs, debug-only values, or personal location details.

| Slot | Screen | Purpose | Required State |
| --- | --- | --- | --- |
| 1 | Identify | Show image/audio identification entry point. | App connected to a seeded or staging backend. |
| 2 | Identify result | Show candidate species and save action. | Use non-sensitive sample media. |
| 3 | Species guide | Show searchable species list. | Seeded species data loaded. |
| 4 | Species detail + hotspot | Show field-guide depth and hotspot recommendation. | Select a species with hotspot data. |
| 5 | Records | Show saved observation records. | Seed at least one non-personal observation. |
| 6 | Trips | Show trip planning inputs and generated plan. | Use a common species and public region. |
| 7 | Native map | Show marker/route context. | Use non-sensitive public hotspot coordinates. |
| 8 | Status/settings | Optional; use only if explaining environment setup to testers. | Hide private production URLs if screenshots are public. |

## Graphic Asset Checklist

- Launcher icon is already configured in the Android project.
- Feature graphic is still needed before a polished public listing.
- Phone screenshots are still needed from the current release candidate.
- Tablet screenshots are optional unless tablet support becomes a release goal.
- Do not use copyrighted wildlife photos unless licensing is verified.

## Review Notes

Before submitting an internal test release:

1. Confirm store copy matches the features actually enabled in the selected backend environment.
2. Confirm screenshots do not expose private API URLs, keys, personal location, or tester accounts.
3. Confirm the Data safety answers match `docs/privacy-and-permissions.md` and the privacy policy draft.
4. Confirm release notes include version name, version code, commit SHA, CI run URL, backend environment, and known limitations.
