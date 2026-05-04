# Pet Records Android

Android client for browsing pets, searching veterinary visit records, uploading receipt PDFs/images, and viewing weight history. The app is built with Jetpack Compose and talks to a backend API that handles OCR and record parsing.

## What it does

- Browse pets and search by name.
- Open a pet's weight trend chart.
- Search visit records by free text.
- Open a record detail screen with parsed fields, line items, reminders, and the source PDF.
- Upload PDFs or captured receipt images.
- Queue uploads through WorkManager so they can retry after failures.
- Store a JWT in-app for APIs that require bearer auth.

## Tech stack

- Kotlin
- Jetpack Compose + Navigation Compose
- Material 3
- Hilt
- Retrofit + OkHttp
- Room
- DataStore
- WorkManager
- Vico charts
- `androidx.pdf` viewer

## Requirements

- Android Studio with Android SDK 36
- JDK 17
- An Android device or emulator running API 28+
- A compatible backend API reachable from the emulator/device

## Project setup

1. Clone the repo.
2. Open it in Android Studio.
3. Make sure JDK 17 is selected for Gradle.
4. Start the backend API.
5. Verify the API base URL in `app/build.gradle.kts`.
6. Run the `app` configuration on an emulator or device.

The current debug build points at:

```text
http://10.0.2.2:3000/api/
```

`10.0.2.2` is the Android emulator alias for the host machine. If you run on a physical device, or your backend uses another host/port, update `BuildConfig.API_BASE_URL` in `app/build.gradle.kts`.

## Authentication

The app can attach a bearer token to every request through `AuthInterceptor`. To configure it:

1. Open the `Settings` tab.
2. Paste a JWT.
3. Save it.

If no token is saved, requests are sent without an `Authorization` header.

## Upload flow

- The `Upload` tab accepts either:
  - one or more existing PDFs/images from the document picker
  - captured receipt pages from the camera
- Selected files are added to a local upload queue.
- A `WorkManager` job processes queued uploads and retries on failure.
- Upload metadata currently supports `petId`, `visitDate`, and `ocrPageCount`.

## Screens

- `Pets`: fetches pets from the API and opens weight history for a selected pet.
- `Search`: searches parsed visit records.
- `Upload`: queues receipt/document uploads.
- `Settings`: saves the JWT used by API calls.
- `Weight`: renders a line chart of weight measurements.
- `Record`: shows visit details and embeds the PDF viewer.

## Backend API assumptions

The app currently expects endpoints like:

- `GET /pets`
- `GET /records/search`
- `GET /weights/pets/{petId}`
- `GET /documents/{id}`
- `GET /documents/{id}/file`
- `DELETE /documents/{id}`
- `POST /documents/upload`
- `POST /documents/upload-images`

The network config currently allows cleartext HTTP traffic to `10.0.2.2` for local development in `app/src/main/res/xml/network_security_config.xml`.

## Local data

Room persists:

- pets
- cached search visits
- weight points
- pending uploads

The database name is `pet-records.db`.

## Architecture

The codebase is organized by feature and shared core layers:

- `app/src/main/java/com/joshfeldman/petrecords/feature`: Compose screens and view models
- `app/src/main/java/com/joshfeldman/petrecords/core/data`: repositories, Room, auth storage
- `app/src/main/java/com/joshfeldman/petrecords/core/network`: Retrofit API and DTOs
- `app/src/main/java/com/joshfeldman/petrecords/core/navigation`: app nav graph
- `app/src/main/java/com/joshfeldman/petrecords/work`: background upload worker

## Build notes

- `compileSdk = 36`
- `minSdk = 28`
- `targetSdk = 36`
- Kotlin source/target compatibility is Java 17

To verify the project compiles:

```bash
./gradlew :app:compileDebugKotlin
```

## Current gaps

- No root-level automated test documentation yet.
- The API base URL is hardcoded in the debug build config rather than injected from local environment.
- The README assumes the backend contract defined in `app/src/main/java/com/joshfeldman/petrecords/core/network/PetRecordsApi.kt`.
