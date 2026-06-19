# Privacy Policy Draft

This is a working privacy-policy draft for WildTrail Android internal testing. Review it with the project owner before publishing it at a public URL or linking it from Google Play.

## Overview

WildTrail is a wildlife identification, observation-record, and trip-planning app. The app connects to a configured WildTrail backend API to process identification requests, load species and hotspot data, save observation records, and generate trip plans.

## Information You Provide

WildTrail may send the following information to the configured backend when you choose to use the related feature:

- Images selected for wildlife identification.
- Audio recordings created for wildlife identification.
- Observation records saved from identification results, including species information, confidence, media type, timestamp, optional note, and optional location label.
- Trip-planning inputs such as selected species, region, number of days, daily travel hours, and mobility mode.

## Device Permissions

WildTrail requests permissions only for app features that need them:

- Microphone access is used when you start audio identification.
- Location access is used for native map and route context when you grant it.
- Network access is used to connect to the backend API and map services.

## How Information Is Used

The information above is used to provide wildlife identification, field-guide information, saved records, trip planning, native map context, and backend health checks. Identification results may be uncertain and should be treated as supporting information for field exploration.

## Sharing

Do not share user-provided media, observation records, or location-related information with third parties unless the backend operator has explicitly documented that sharing. Google Maps services may process map-related requests according to Google's applicable terms when native map features are used.

## Retention

Retention depends on the configured WildTrail backend environment. For internal testing, use seeded or non-sensitive test data where possible. Define production retention rules before widening tester access.

## Security

Keep API keys, release signing keys, backend credentials, and production URLs out of the Git repository. Use private local configuration, CI secrets, and the release signing process documented in this repository.

## User Choices

You can avoid sending image or audio media by not starting identification. You can deny microphone or location permissions and continue using the rest of the app where those permissions are not required. Backend-stored records require backend-side deletion support if removal is needed.

## Contact

Add the project owner's support email or contact URL before publishing this policy.

## Pre-Publication Checklist

1. Replace this draft label with the final policy title.
2. Add public contact information.
3. Add backend retention and deletion details.
4. Confirm Google Play Data safety answers match this policy and `docs/privacy-and-permissions.md`.
5. Publish at a stable HTTPS URL before public Play Store distribution.
