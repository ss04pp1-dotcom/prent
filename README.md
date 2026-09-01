# Parental Care — Native Android App

A **consent-based screenshot monitoring** parental control app for Android 14+,
written in **Kotlin + Jetpack Compose**. Two separate APKs (parent + child) are
built from a single Gradle root project, sharing 6 core modules.

> **Strict anti-spyware stance.** No hidden surveillance, no keylogging, no
> accessibility abuse, no consent bypass, no root exploits. The child is
> **always** notified when monitoring is active; every screenshot requires a
> fresh system-level MediaProjection consent dialog; child must be paired
> with the parent via a one-time cryptographically secure QR token.

## Project structure

```
parental-care-android/
├── app-parent/           Parent app (dark premium theme #0F172A / #7C3AED)
├── app-child/            Child app (light theme #F5F6FA / #5B4DFF)
├── core/
│   ├── common/           Result<T>, AppError, SecureTokenGenerator, TimeUtils, Ownership
│   ├── design/           Compose theme (parent dark + child light variants), SharedComponents
│   ├── security/         AES-GCM ScreenshotEncryptor, KeystoreManager, PairingToken + PairingSerializer, ScreenshotRequest + Validator + NonceRegistry
│   ├── firebase/         FirebaseInitializer (Auth + Firestore + Storage + FCM + App Check Play Integrity)
│   ├── data/             Firestore models + repositories, Hilt DataModule with EncryptedSharedPreferences
│   └── notifications/    NotificationChannelInitializer, BaseFcmService, NotificationPermission helper
├── firebase/
│   ├── firestore.rules   Deny-by-default multi-tenant rules with status-transition enforcement
│   ├── storage.rules      Path-bound ownership check, content-type/size/regex constraints
│   ├── firebase.json      Composite indexes + emulator config
│   ├── SETUP.md           How to wire a real Firebase project
│   └── app_check_config.md
├── docs/
│   ├── README.md         Quick-start guide
│   ├── ARCHITECTURE.md   Capture pipeline + pairing flow + data model + module graph
│   ├── SECURITY.md       Forbidden behaviors + permissions table + crypto pipeline + security test matrix
│   └── BUILD.md
└── gradle/
    └── libs.versions.toml  Centralized version catalog
```

## Tech stack

- **Kotlin 2.0.21** + Coroutines 1.9 + Serialization 1.7
- **Jetpack Compose** via BOM 2024.12.01
- **Hilt 2.52** + Hilt-Navigation-Compose 1.2
- **Firebase BoM 33.5** — Auth, Firestore, Storage, Messaging, App Check (Play Integrity)
- **AGP 8.7.2**, compileSdk 35, minSdk 26, targetSdk 35
- **ZXing** for QR encoding/decoding
- **Coil 2.7** for image loading
- **Timber 5.0.1** for logging (stripped in release build)
- **AndroidX EncryptedSharedPreferences** + **Android Keystore** for at-rest crypto
- **WorkManager 2.10** (Hilt-aware)

## Screenshot capture pipeline

```
Parent App                Firestore              Child App                  Cloud Storage
─────────                ─────────              ─────────                  ─────────────
RequestScreenshot UI
        │
        ▼
ScreenshotRequestRepository.createRequest
        │
        ▼ (writes doc)
   screenshotRequests/{requestId}     ────────►  FCM "SCREENSHOT_REQUEST" push
   (status=REQUESTED)                              │
                                                  ▼
                                            IncomingRequestHandler
                                            (replay-protected via NonceRegistry)
                                                  │
                                                  ▼
                                            RequestScreen (UI)
                                                  │ user taps "Take Screenshot"
                                                  ▼
                                            ActivityResult MediaProjection consent
                                                  │ system dialog (NEVER bypassed)
                                                  ▼
                                            CaptureForegroundService.start
                                            (FOREGROUND_SERVICE_MEDIA_PROJECTION)
                                                  │
                                                  ▼
                                            ScreenshotProjectionManager.startCapture
                                            (VirtualDisplay + ImageReader)
                                                  │
                                                  ▼
                                            ScreenshotEncoder.compress
                                            (WebP/JPEG, max 1080px, 70-85 quality)
                                                  │
                                                  ▼
                                            ScreenshotEncryptor.encrypt
                                            (AES-GCM, fresh content key, Keystore-wrapped)
                                                  │
                                                  ▼
                                            ScreenshotRepository.upload
                                                  │ uploads .enc blob + writes Firestore metadata
                                                  ▼
                                          screenshots/{screenshotId}     ──►  families/{familyId}/screenshots/{deviceId}/{screenshotId}.enc
                                          (status=UPLOADED)                  (encrypted blob — never public URL)
                                                  │
                                                  ▼
                                            FCM "SCREENSHOT_RECEIVED" to parent
                                                  │
                                                  ▼
                                            ParentFcmService → notification "New screenshot received"
                                                  │
                                                  ▼
                                            ScreenshotInbox → ScreenshotViewer
                                            (downloads .enc blob, decrypts with Keystore master key)
                                                  │
                                                  ▼
                                            Permanent delete on parent action OR retention expiry
                                            (24h default; 1h/6h/24h/7d options)
```

## Setup (developer)

1. **Clone & open** in Android Studio Ladybug+ (or newer).
2. **Create a Firebase project** at <https://console.firebase.google.com>.
3. **Register two Android apps** in the project:
   - Package name `com.parentalcare.parent` (debug: `com.parentalcare.parent.debug`)
   - Package name `com.parentalcare.child` (debug: `com.parentalcare.child.debug`)
4. **Download `google-services.json`** for each app and drop it in:
   - `app-parent/google-services.json`
   - `app-child/google-services.json`
   (templates are at `app-parent/google-services.json.template` and
   `app-child/google-services.json.template`.)
5. **Enable Firebase services**:
   - Authentication → Email/Password
   - Firestore (production mode, then deploy `firebase/firestore.rules`)
   - Storage (production mode, then deploy `firebase/storage.rules`)
   - Cloud Messaging (no extra config)
   - App Check → register Play Integrity (see `firebase/app_check_config.md`)
6. **Deploy rules + indexes** from `firebase/`:
   ```bash
   cd firebase/
   firebase deploy --only firestore:rules,firestore:indexes,storage
   ```
7. **Build & run**:
   ```bash
   ./gradlew :app-parent:assembleDebug
   ./gradlew :app-child:assembleDebug
   ```
   Install the parent APK on the parent's phone, the child APK on the child's
   phone, then pair them via the QR code in `Parent → Add Child Device`.

## Testing

```bash
./gradlew test                        # Unit tests (core:security)
./gradlew connectedAndroidTest        # Instrumentation tests (requires device/emulator)
```

Existing unit tests:
- `PairingTokenTest` — issue/serialize/deserialize/verify/consume cycle
- `CommonUtilsTest` — SecureTokenGenerator, NonceRegistry, ScreenshotRequestValidator, sha256Hex

## Security model — at a glance

| Concern                     | Defense                                                                                                     |
|----------------------------|-------------------------------------------------------------------------------------------------------------|
| Hidden surveillance         | Persistent always-on foreground notification on child; monitoring status visible everywhere                |
| Consent bypass              | Every screenshot requires fresh MediaProjection system dialog (no auto-reuse across captures)              |
| Replay attack               | NonceRegistry on child + nonce in Firestore doc + `expiresAt` enforced in rules                           |
| Cross-family reads          | Firestore rules use `isParentOf(familyId)` + `isChildOf(familyId, deviceId)` helpers                       |
| Storage enumeration         | Storage paths are `families/{familyId}/screenshots/{childDeviceId}/{screenshotId}.enc`, ownership-checked  |
| Screenshot leak from URL    | Files are AES-GCM encrypted at the application layer; the wrapped content key is Keystore-backed           |
| Public download URLs        | No `getDownloadUrl()` calls; signed URLs only via the parent app after auth                                |
| Anti-tamper                 | App Check (Play Integrity) blocks requests from non-genuine app binaries                                    |
| Misconfigured rules         | Deny-by-default — any rule that fails to match returns `false`                                            |
| Excess permissions          | No READ_CONTACTS / READ_SMS / RECORD_AUDIO / CAMERA / SYSTEM_ALERT_WINDOW / accessibility                   |

See `docs/SECURITY.md` for the full threat model, test matrix, and forbidden behaviors.

## License

Proprietary — © 2024 Parental Care. All rights reserved.
