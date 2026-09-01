# Parental Care — Android

**Production-quality, consent-based, screenshot-only parental control app for Android.**

Two native Kotlin + Jetpack Compose apps backed by Firebase:

| App | Package | Theme | Purpose |
|-----|---------|-------|---------|
| **Parent** | `com.parentalcare.parent` | Dark premium (`#0F172A` + `#7C3AED`) | Pair devices, request screenshots, view inbox, manage settings |
| **Child** | `com.parentalcare.child` | Light + purple (`#F5F6FA` + `#5B4DFF`) | Onboard, pair via QR, accept screenshot requests, capture via MediaProjection |

> **This is NOT spyware.** The child is always informed when monitoring is active, when a request arrives, and when a screenshot is captured. The app never bypasses Android's MediaProjection consent dialog. It never reads messages, never logs keystrokes, never uses Accessibility services, never records audio or camera.

---

## Project structure

```
parental-care-android/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml          ← version catalog
│   └── wrapper/
├── gradle.properties
├── gradlew
├── local.properties.template
├── .gitignore
├── firebase/
│   ├── firestore.rules             ← multi-tenant, deny-by-default
│   ├── storage.rules               ← ownership-bound, no public URLs
│   ├── firebase.json               ← indexes + emulator config
│   ├── SETUP.md                    ← how to wire a real Firebase project
│   └── app_check_config.md
├── core/
│   ├── common/                     ← Result<T>, AppError, time utils, constants, redactor
│   ├── design/                      ← Compose theme: Parent dark + Child light
│   ├── security/                    ← AES-GCM + Android Keystore + pairing tokens + nonce registry
│   ├── firebase/                    ← Firebase init + App Check + path helpers
│   ├── data/                        ← Firestore models + repositories (devices, screenshots, requests, pairing, auth, activity)
│   └── notifications/               ← FCM base service + notification channels + permission helper
├── app-parent/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/parentalcare/parent/
│   │   │   ├── MainActivity.kt
│   │   │   ├── app/ParentApplication.kt
│   │   │   ├── nav/ParentRootNav.kt
│   │   │   ├── ui/screen/          ← 15 Compose screens matching the design
│   │   │   ├── pairing/PairingIssuer.kt
│   │   │   ├── qr/QrCodeGenerator.kt
│   │   │   └── fcm/ParentFcmService.kt
│   │   └── res/
│   │       ├── values/{strings,themes,colors}.xml
│   │       ├── drawable/ic_launcher_foreground.xml
│   │       ├── mipmap-anydpi-v26/ic_launcher*.xml
│   │       └── xml/{network_security_config,data_extraction_rules}.xml
│   ├── proguard-rules.pro
│   ├── google-services.json.template
│   └── build.gradle.kts
├── app-child/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/parentalcare/child/
│   │   │   ├── MainActivity.kt
│   │   │   ├── app/ChildApplication.kt
│   │   │   ├── nav/ChildRootNav.kt
│   │   │   ├── ui/screen/          ← 15 Compose screens matching the design
│   │   │   ├── mediaprojection/    ← Native capture layer
│   │   │   │   ├── MediaProjectionManager.kt
│   │   │   │   ├── ScreenshotEncoder.kt
│   │   │   │   ├── CaptureForegroundService.kt
│   │   │   │   └── ScreenCaptureManager.kt
│   │   │   ├── service/MonitoringForegroundService.kt
│   │   │   ├── fcm/ChildFcmService.kt
│   │   │   └── pipeline/IncomingRequestHandler.kt
│   │   └── res/ (same shape as parent)
│   ├── proguard-rules.pro
│   ├── google-services.json.template
│   └── build.gradle.kts
└── docs/
    ├── README.md                   ← this file
    ├── ARCHITECTURE.md
    ├── SECURITY.md
    └── BUILD.md
```

---

## Tech stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose (Material 3), Navigation-Compose, Material Icons Extended |
| Async | Coroutines + Flow, kotlinx-serialization-json |
| DI | Hilt + WorkManager-Hilt bridge |
| Local storage | DataStore + EncryptedSharedPreferences (`androidx.security.crypto`) |
| Auth | Firebase Auth (parent email/password) + child device identity |
| Backend | Firebase Firestore + Storage + Cloud Messaging + App Check |
| Native | Android `MediaProjection` API for screen capture (no root, no Accessibility abuse) |
| Crypto | AES-GCM content-key wrapping via `AndroidKeystore` master keys |
| QR | ZXing core + zxing-android-embedded |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

---

## Quick start

1. **Clone / unzip** the project.
2. **Open in Android Studio** (Hedgehog or newer).
3. **Create a Firebase project** — see `firebase/SETUP.md`.
4. Drop your real `google-services.json` into both `app-parent/` and `app-child/` (templates provided).
5. Select the `app-parent` or `app-child` run configuration.
6. Run on a device (parent or child) or emulator (API 26+).

For full build instructions, see `docs/BUILD.md`.

---

## Security highlights

- **Multi-tenant Firestore** with deny-by-default rules — every document path embeds `familyId`, and every rule cross-checks `request.auth.uid` against ownership. Cross-family reads are rejected.
- **Screenshot encryption**: every screenshot is encrypted with a fresh 256-bit content key (AES-GCM, 96-bit IV, 128-bit auth tag). The content key is wrapped by an `AndroidKeystore`-backed master key scoped per `familyId`. The Keystore master never leaves the secure enclave.
- **Storage paths are ownership-bound**: `families/{familyId}/screenshots/{childDeviceId}/{screenshotId}.enc`. Storage rules reject writes from a child to a foreign `childDeviceId`, reads from non-parent users, and public URL generation.
- **Pairing tokens**: one-time, 2-minute TTL, 32-byte base32 opaque + nonce, redeemed atomically via a Firestore transaction. Replay attempts are rejected.
- **Anti-replay for screenshot requests**: each request carries a 16-byte nonce; the child maintains an LRU `NonceRegistry` and silently drops duplicates. Requests expire after 5 minutes; child rejects expired ones.
- **No public download URLs**: Storage rules forbid `getPublicDownloadUrl` patterns; only Firebase SDK authenticated access is permitted.
- **Foreground services**: child app runs `CaptureForegroundService` (type `mediaProjection`) for the duration of one capture only, and `MonitoringForegroundService` (type `dataSync`) as a persistent always-visible indicator.
- **Cleartext traffic forbidden**: both apps ship a `network_security_config.xml` with `cleartextTrafficPermitted="false"`.
- **Cloud backup disabled**: `android:allowBackup="false"` + `data_extraction_rules.xml` excludes all sensitive domains.
- **Logging policy**: production builds strip Timber `v/d/i/w` via ProGuard `assumenosideeffects`. `Timber.e` is kept. `Redactor` always redacts opaque tokens before logging.
- **Permissions**: only the absolute minimum — `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE*`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED` (optional). Never requests contacts/SMS/camera/microphone/accessibility.
- **App Check**: Play Integrity attestation required for every Firebase call.

Full security write-up in `docs/SECURITY.md`. Security testing checklist at the bottom of the same file.

---

## Privacy defaults

- No live streaming. No audio. No camera. No message reading. No keyboard monitoring. No contact reading. No location.
- Screenshot retention: default 24 hours, options 1h / 6h / 24h / 7d. Expired screenshots are deleted from Storage + Firestore + local cache via a periodic WorkManager sweep (or Cloud Function cron).
- Sensitive screenshots never appear in the device's photo gallery unless the parent explicitly downloads one.
- Parental biometric lock (optional) requires biometric/PIN before viewing screenshots.

---

## Development order (per the spec)

The spec defines 16 phases. The codebase implements phases 1–14 fully. Phases 15 (testing) and 16 (release build) are pre-release activities for the developer — see `docs/SECURITY.md` for the test matrix.

---

## License

Proprietary — © 2024 Parental Care. All rights reserved.
