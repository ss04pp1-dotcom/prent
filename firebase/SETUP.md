# Firebase Project Setup

## 1. Create the Firebase project

1. Go to https://console.firebase.google.com/
2. Click "Add project" → name it `parental-care-<your-suffix>`
3. (Optional) Enable Google Analytics if you want Crashlytics.

## 2. Enable products

In the Firebase Console:

- **Authentication** → Sign-in method → Email/Password → Enable.
- **Firestore Database** → Create database (production mode, region: `us-central1` or your nearest).
- **Storage** → Get started (production mode, region same as Firestore).
- **Cloud Messaging** → Auto-enabled.
- **App Check** → Register both apps (parent + child). Use Play Integrity as the attestation provider.

## 3. Add Android apps

### Parent app
1. Click the Android icon ("Add app" → Android).
2. Package name: `com.parentalcare.parent`
3. Debug SHA-1: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1`
4. Download `google-services.json` → place at `app-parent/google-services.json`.

### Child app
1. Add a second Android app to the same project.
2. Package name: `com.parentalcare.child`
3. SHA-1 (same debug keystore is fine).
4. Download `google-services.json` → place at `app-child/google-services.json`.

## 4. Deploy rules

```bash
# from project root
cd firebase
firebase use --add  # pick your project
firebase deploy --only firestore:rules,storage:rules
```

## 5. Indexes

The composite indexes declared in `firebase.json` deploy automatically with:

```bash
firebase deploy --only firestore:indexes
```

## 6. Optional: emulators (local dev)

```bash
cd firebase
firebase emulators:start
```

Then point your app at `http://10.0.2.2:8080` (Firestore) and `http://10.0.2.2:9199` (Storage) using Firebase's `useEmulator()` API.

## 7. Optional: Cloud Functions

The repo ships without functions, but you may add them later for:
- Server-side pairing token signing (so the QR code carries a signature the child can verify without trusting Firestore rules).
- Cron-based screenshot retention sweep (alternative to client WorkManager).
- FCM "screenshot received" sender from a Firestore trigger on `screenshots/`.

Place function code under `firebase/functions/` and deploy with `firebase deploy --only functions`.
