# Build & Run

## Prerequisites

- **JDK 17** — `java -version` should print `17.x`.
- **Android Studio Ladybug (2024.2) or newer** (or just the Android SDK + Gradle 8.9+).
- **Android SDK 35** (compileSdk) with platform-tools 35.x.
- **Android SDK 26+ minimum device** for testing (real device strongly preferred — MediaProjection does not work in stock emulators on older AVDs; works on API 34+ emulator with Google APIs).
- **Firebase project** — see `firebase/SETUP.md`.

## Initial setup

```bash
# 1. Make gradlew executable
chmod +x ./gradlew

# 2. Copy local.properties template and adjust SDK path
cp local.properties.template local.properties
$EDITOR local.properties   # set sdk.dir to your Android SDK path

# 3. Drop google-services.json into both apps (after creating a Firebase project — see firebase/SETUP.md)
cp /path/to/parent-google-services.json app-parent/google-services.json
cp /path/to/child-google-services.json  app-child/google-services.json

# 4. (Optional) Deploy Firestore + Storage rules
cd firebase
firebase use --add   # pick your project
firebase deploy --only firestore:rules,storage:rules,firestore:indexes
cd ..
```

## Build

### From Android Studio

1. `File` → `Open` → select the project root.
2. Wait for Gradle sync.
3. Pick a build variant in the lower-left "Build Variants" pane:
   - `app-parent` → `debug` or `release`
   - `app-child` → `debug` or `release`
4. Click ▶️ Run on a connected device or emulator.

### From CLI

```bash
# Debug builds (parent + child)
./gradlew :app-parent:assembleDebug
./gradlew :app-child:assembleDebug

# Release builds (with R8 + resource shrinking; uses debug-signing config for now)
./gradlew :app-parent:assembleRelease
./gradlew :app-child:assembleRelease

# Run unit tests
./gradlew :core:security:testDebugUnitTest
./gradlew :core:common:testDebugUnitTest

# Lint
./gradlew :app-parent:lintDebug
./gradlew :app-child:lintDebug
```

## Test on a device

1. Enable USB debugging on the child's Android device.
2. `adb devices` should list it.
3. Select the `app-child` build variant → Run.
4. On the parent device / emulator:
   - Select the `app-parent` build variant → Run.
   - Sign in (or use the demo account created during Firebase Auth setup).
   - Tap "Settings → Connected Devices → Add New Device" → QR shows.
5. On the child device:
   - Tap "Get Started" → continue through permissions.
   - On the pairing screen, scan the parent's QR (or use zxing-android-embedded scanner once wired).
   - Pairing success → continue → grant MediaProjection consent.
   - "Monitoring Active" appears.
6. On parent: tap "Request Screenshot".
7. On child: incoming request notification → tap "Take Screenshot" → system dialog → "Start now" → capture happens → "Screenshot Sent".
8. On parent: notification "New screenshot received" → open inbox → tap screenshot → viewer → "Delete Permanently".

## Release signing

The shipped `release` build type uses the debug keystore by default so the project builds out of the box. For a real release:

1. Generate a real upload key:

   ```bash
   keytool -genkeypair -v -keystore parental-care.keystore -alias parental-care \
     -keyalg RSA -keysize 4096 -validity 10000
   ```

2. Add a `keystore.properties` file at the project root (git-ignored):

   ```
   storeFile=/absolute/path/to/parental-care.keystore
   storePassword=...
   keyAlias=parental-care
   keyPassword=...
   ```

3. Update `app-parent/build.gradle.kts` and `app-child/build.gradle.kts` to read from `keystore.properties`:

   ```kotlin
   val keystoreProperties = java.util.Properties().apply {
       val f = rootProject.file("keystore.properties")
       if (f.exists()) load(f.inputStream())
   }
   android {
       signingConfigs {
           create("release") {
               keyAlias = keystoreProperties["keyAlias"] as String?
               keyPassword = keystoreProperties["keyPassword"] as String?
               storeFile = file(keystoreProperties["storeFile"] as String? ?: "")
               storePassword = keystoreProperties["storePassword"] as String?
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               ...
           }
       }
   }
   ```

4. `./gradlew :app-parent:assembleRelease :app-child:assembleRelease`

## CI recommendations

- Run `./gradlew testDebugUnitTest lintDebug assembleDebug` on every PR.
- Run `./gradlew test assembleRelease` on every release branch.
- Use the official `cimg/android:2024.07` CircleCI image or `androidsdk/android-34` Docker.

## Known limitations / TODOs for production

- **Pairing via QR scanner**: the parent app generates the QR; the child app currently shows a placeholder viewfinder. Wire the actual scan via `zxing-android-embedded`'s `BarcodeScanner` and call `PairingRepository.redeemPairingToken(PairingSerializer.decode(scannedString))`. The plumbing is in place.
- **Parent app screenshot viewer** uses a placeholder "5:42" mock for the screenshot itself. Wire to `ScreenshotRepository.fetchDecryptedBitmap(doc)` and render the result via Coil's `Image(bitmap=...)`.
- **Biometric unlock**: `androidx.biometric` dependency is in, but the gating logic is not wired. Wrap the viewer with a `BiometricPrompt` when `FamilyDoc.biometricLockEnabled == true`.
- **Cron retention sweep**: a Cloud Function should run `sweepExpired(familyId)` for every family every hour. Alternatively, schedule a periodic `WorkManager` task in the parent app that iterates the parent's families.
- **FCM trigger for screenshot-received notification**: either add a Cloud Function trigger on `screenshots/{screenshotId}` write that calls `FirebaseMessaging.send()` to the parent's FCM token, or send the FCM directly from the child after upload (less secure — child could spam).
