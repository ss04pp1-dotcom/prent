// ============================================================================
// Firebase App Check configuration reference.
// ----------------------------------------------------------------------------
// Enable App Check in the Firebase Console for:
//   - Firestore
//   - Storage
//   - Cloud Messaging (server-side)
//
// For the Android client, the app registers the Play Integrity attestation
// provider during [FirebaseInitializer.installAppCheck]. The Play Integrity
// attestation travels with each Firebase request and is validated server-side.
//
// For local development:
//   1. Generate a debug secret:
//        https://firebase.google.com/docs/app-check/android/play-integrity-provider#debug-secret
//   2. Place it in debug FirebaseAppCheck installation via:
//        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
//        firebaseAppCheck.installAppCheckProviderFactory(
//            DebugAppCheckProviderFactory.getOneInstance());
//
// IMPORTANT:
//   App Check is NOT a substitute for Firebase Authentication + Security Rules.
//   It is an additional layer that rejects clients without a valid attestation
//   token. Authorization is still enforced by the rules in firestore.rules /
//   storage.rules.
// ============================================================================
