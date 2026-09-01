# Security Model

> Security is more important than convenience. The application must NEVER
> behave like hidden spyware.

## Forbidden behaviors (HARD RULES)

The codebase does **NOT** implement, and **MUST NOT** implement, any of the following:

- Hidden surveillance / covert monitoring
- Keylogging or any form of input capture
- Password / credential extraction
- Reading WhatsApp / Messenger / SMS / contact data
- Accessibility-service abuse (reading other apps via `BIND_ACCESSIBILITY_SERVICE`)
- Hidden microphone or camera recording
- Root exploits or any use of root
- Android security model bypasses
- Bypassing the MediaProjection system consent dialog
- Capturing when Android has revoked the required authorization
- Persistent screen capture beyond a single shot
- Any mechanism designed to conceal monitoring from the child

If a future PR adds any of these, it must be rejected during review.

## Permissions — what we request and why

| Permission | Why we need it |
|------------|----------------|
| `INTERNET` | Talk to Firebase (Auth / Firestore / Storage / FCM) |
| `ACCESS_NETWORK_STATE` | Detect offline state for "your child is offline" UI |
| `POST_NOTIFICATIONS` (Android 13+) | Show screenshot request / received notifications |
| `FOREGROUND_SERVICE` | Run the monitoring + capture foreground services |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` (Android 14+) | Required type for `CaptureForegroundService` |
| `FOREGROUND_SERVICE_DATA_SYNC` (Android 14+) | Required type for `MonitoringForegroundService` |
| `WAKE_LOCK` | Keep CPU running for the ~5s of one capture |
| `RECEIVE_BOOT_COMPLETED` | Optional — only used if the user explicitly enables "Start on Boot" in Settings |
| `USE_BIOMETRIC` | Optional — for parent biometric unlock before viewing screenshots |

### Permissions we will NEVER request

- `READ_CONTACTS`, `READ_CALL_LOG`, `READ_SMS`, `READ_PHONE_STATE`
- `RECORD_AUDIO`, `CAMERA`
- `SYSTEM_ALERT_WINDOW` (draw over other apps)
- `REQUEST_INSTALL_PACKAGES`
- `BIND_ACCESSIBILITY_SERVICE`
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` (unless the user explicitly opts in for a future optional feature)

## Multi-tenant isolation

Every Firestore path embeds the family ID. Security rules in `firebase/firestore.rules` enforce:

1. **Parent** can read only their own family — verified by comparing `request.auth.uid` to `families/{familyId}.parentUserId`.
2. **Child** can read only its own device's requests — verified by comparing `request.auth.uid` to `families/{familyId}/devices/{deviceId}.ownerMemberId`.
3. **Cross-family access is impossible** — a malicious authenticated user querying `families/<someone_else>/screenshots` gets a permission-denied response from Firestore before any data reaches the client.

The same logic is mirrored in Storage rules:

- `families/{familyId}/screenshots/{childDeviceId}/{screenshotId}.enc` — child can write only to its own device path; parent can read/delete only their own family's path.

## Screenshot encryption pipeline

```
capture (Bitmap)
   │
   ▼
ScreenshotEncoder.encode  (downscale to ≤1080×1920, JPEG q80, ≤600 KB)
   │
   ▼ bytes
ScreenshotEncryptor.encrypt
   │
   ├─►  fresh 256-bit content key (SecureRandom)
   ├─►  fresh 96-bit IV (SecureRandom)
   ├─►  AES/GCM/NoPadding(content_key, IV) → ciphertext (128-bit auth tag)
   └─►  KeystoreManager.wrap(content_key, familyId) → "iv:ciphertext" base64
       (master key in AndroidKeystore, alias "parentalcare.<familyId>")
   │
   ▼ EncryptedPayload { iv, wrappedKey, ciphertext }
   │
   ▼
Storage putBytes(ciphertext)  → families/{fid}/screenshots/{devId}/{shotId}.enc
Firestore.set(ScreenshotDoc { iv, wrappedKey, storagePath, ... })
```

### What this defends against

- **Compromised Storage access** — a leaked `getDownloadUrl` is useless without the IV + wrapped key + Keystore master key on an authorized device.
- **Cross-family reads** — Storage rules reject the request before any byte reaches the client.
- **Replay of old screenshots** — each screenshot has a unique `screenshotId` and its own ephemeral content key.

### What this does NOT defend against

- **Compromised parent device** — the parent's device legitimately decrypts its own screenshots. If the parent's device is rooted, an attacker with full disk access could extract both the Storage data and the Keystore-wrapped keys. This is accepted; we cannot prevent the parent from decrypting what they own.

## Pairing token security

- **32-byte base32 opaque value** (`SecureTokenGenerator.generateOpaqueToken`) — no PII inside the QR code.
- **2-minute TTL** — `pairingTokens/{tokenId}.expiresAt` enforced by rule.
- **One-time use** — Firestore transaction checks `isConsumed == false` and flips to `true` atomically.
- **Constant-time-ish equality check** via `PairingToken.verify`.
- **SHA-256 of opaque** is the document ID — so a malicious user trying to scan a forged QR still hits a document that won't verify against the stored one.

## Screenshot request anti-replay

- Each request carries a 16-byte `nonce`.
- The child maintains an in-memory `NonceRegistry` (LRU, 1024 entries, 1h TTL).
- A request whose `nonce` was seen in the last hour is silently dropped.
- Requests past their `expiresAt` (default 5 min, max 30 min) are rejected by the child AND by Firestore rules.

## Authentication + App Check

- **Firebase Authentication** — parent signs in with email/password. Child authenticates via Firebase Anonymous Auth (or a paired email) on first launch.
- **Firebase App Check** with **Play Integrity** — every Firebase request carries an attestation token. Tokens are validated server-side. Tampered / repackaged apps fail attestation and are rejected.
- **App Check is NOT a substitute for security rules** — it's an additional layer. Authorization is always enforced by `firestore.rules` + `storage.rules`.

## Local storage

- `EncryptedSharedPreferences` ("pc_secure_prefs") via `androidx.security.crypto` MasterKey (AES-256-GCM, Keystore-backed). Stores the local family id / device id / current parent account.
- `android:allowBackup="false"` + `data_extraction_rules.xml` excludes all sensitive domains (root, database, sharedpref, external, file) for both cloud backup and D2D transfer.
- Sensitive screenshots are NEVER persisted to the device's gallery unless the parent explicitly downloads one.
- Temporary decrypted bytes live in RAM only; viewers should `recycle()` bitmaps on exit.

## Network security

- `network_security_config.xml` forbids all cleartext (`cleartextTrafficPermitted="false"`).
- Firebase SDK uses HTTPS by default for all endpoints.
- Certificate pinning is intentionally NOT shipped by default — it breaks easily and is hard to rotate. If you need it, see the commented-out `<domain-config>` block in each app's `network_security_config.xml`.

## Foreground service behavior

- **`CaptureForegroundService`** (type `mediaProjection`) — lives only for the ~5 seconds of one screenshot capture. Started BEFORE `MediaProjectionManager.getMediaProjection()` (Android 14 requirement). Self-stops on capture completion or error.
- **`MonitoringForegroundService`** (type `dataSync`) — persistent while monitoring is enabled. Shows a low-priority notification "Parental Care — Screen capture service active" so the child can always see that monitoring is on.

## Logging policy

| Never log | Safe to log |
|-----------|-------------|
| Screenshot image bytes / image thumbnails | requestId prefix (8 chars) |
| Encryption keys (content, master, wrapped) | screenshotId prefix (8 chars) |
| Firebase ID tokens / refresh tokens | familyId prefix (8 chars) |
| Auth secrets | status code (`UPLOADED`, `FAILED`, ...) |
| Screenshot URLs containing credentials | error category (`Unknown`, `Network`, ...) |
| Child personal content | timestamp (epoch millis) |

Production builds strip Timber `v/d/i/w` calls via R8 `assumenosideeffects`. `Timber.e` is kept for error diagnostics. `Redactor.redact()` is the helper for any value that might be sensitive.

## Anti-tamper surface

The child app surfaces clear status for each of these states instead of pretending monitoring is active:

- Monitoring permission revoked → "Stop Monitoring" banner.
- Notification permission disabled → settings nag.
- Device unpaired → "Device unpaired" full-screen.
- Firebase authentication expired → re-auth screen.
- Storage upload failure → "Could not upload, please check your connection" toast.
- MediaProjection unavailable (revoked by OS) → "Screen capture permission has been revoked" screen.

The app **never** silently circumvents Android restrictions. If the OS revokes MediaProjection, the app stops honoring requests until consent is restored.

## Security testing checklist (run before every release)

| Scenario | Expected result |
|----------|------------------|
| Unauthorized parent access to another family's `families/{fid}` doc | `PERMISSION_DENIED` |
| Unauthorized child access to another device's `screenshotRequests` | `PERMISSION_DENIED` |
| Forged `familyId` in a Firestore query | `PERMISSION_DENIED` |
| Forged `deviceId` in a Firestore query | `PERMISSION_DENIED` |
| Replay: send the same request nonce twice | Second request silently dropped |
| Expired screenshot request reaches child | Child rejects without capturing |
| Forged screenshot request (wrong family) | Validator rejects |
| Screenshot URL shared between two parents | Only the owning parent can decrypt |
| Firebase rule bypass attempts (wildcard queries, listDocuments) | `PERMISSION_DENIED` |
| Authentication expired mid-session | App surfaces re-auth screen |
| Device unpaired at runtime | Child stops honoring requests |
| MediaProjection permission revoked by user | Child stops honoring requests |
| App restart | Pending requests are re-checked for freshness |
| Phone reboot | Monitoring service restarts only if user enabled Start on Boot |
| Android 14 → 10 backwards compatibility | Capture works on API 26+ |
| Storage cleanup on screenshot delete | Both Storage + Firestore removed |
| Local cache cleanup on screenshot delete | In-memory bitmap recycled |
| Attempt to access another family intentionally | `ACCESS DENIED` |
| Forged pairing token (random 32 bytes) | Transaction rejected |
| Expired pairing token scan | Child app surfaces "pairing token expired" |
