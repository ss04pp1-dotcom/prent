# Architecture

## High-level

```
                +-------------------+
                | Parent App        |
                |  (Kotlin/Compose) |
                +----+--------+-----+
                     |        |
            (Auth)   |        |  (Firestore/Storage)
                     v        v
                +-------------------+        +-------------------+
                |   Firebase        +<------>+  Cloud Functions  |
                |   (Auth/Fire/    |        |  (optional — for |
                |    Storage/FCM/  |        |  signed requests, |
                |    App Check)    |        |  retention cron) |
                +----+--------+---+        +-------------------+
                     |        |
                     v        v
                +-------------------+
                | Child App         |
                |  (Kotlin/Compose) |
                +-+------------+----+
                  |            |
                  |            +-- MediaProjectionManager (native)
                  |            +-- ScreenshotEncoder (compress + thumbnail)
                  |            +-- CaptureForegroundService
                  |            +-- MonitoringForegroundService
                  |
                  +-- Hilt graph
                  +-- EncryptedSharedPreferences (session + opaque state)
                  +-- Android Keystore (master keys)
```

## Module dependency graph

```
app-parent  app-child
    \           /
     +--- core:data
            |
            +-- core:firebase
            |       |
            |       +-- core:security
            |               |
            |               +-- core:common
            |
            +-- core:notifications
                    |
                    +-- core:firebase (already)
                    +-- core:common (already)
            +-- core:design
                    |
                    +-- core:common (already)
```

`core:design` is Compose-only — no Firebase / data dependencies. Safe to depend on from any module's preview.

`core:security` has no Firebase dependency (the security layer is pure crypto + Keystore + serializable models). This means the crypto layer can be unit-tested without Android.

`core:firebase` exposes Firebase singletons + path helpers, and depends on `core:common` + `core:security` so it can use `EncryptedPayload` from the crypto layer.

`core:data` depends on all four upstream cores and exposes typed repositories (`DeviceRepository`, `ScreenshotRepository`, `ScreenshotRequestRepository`, `PairingRepository`, `AuthRepository`, `ActivityLogRepository`).

`core:notifications` is the smallest module: `BaseFcmService` + channel initialization + permission helper.

## Capture pipeline (child app)

```
1. Parent → createScreenshotRequest(familyId, childDeviceId)
            Firestore write families/{fid}/screenshotRequests/{reqId}
            Cloud Function (optional) sends FCM to child.

2. Child  ← FCM "SCREENSHOT_REQUEST" payload arrives
            ChildFcmService.onDataMessage(payload)
              → IncomingRequestHandler.handleIncomingRequest(payload)
                 1. seen.isFresh(requestId)  ← drop replay
                 2. requestRepo.getById     ← read authoritative doc
                 3. request.isExpired      ← drop stale
                 4. _active.value = req    ← UI surfaces RequestScreen

3. Child  → User taps "Take Screenshot"
            Launch MediaProjection consent activity (system dialog).
            NEVER bypassed.

4. Child  → onActivityResult(resultCode, data)
            ScreenCaptureManager.capture(resultCode, data)
              1. CaptureForegroundService.start()  ← Android 14 requirement
              2. resolve capture size
              3. projectionManager.startCapture(...)
              4. ImageReader callback → Bitmap
              5. CaptureForegroundService.stop()

5. Child  → ScreenshotRepository.uploadCaptured(bitmap, request)
              1. ScreenshotEncoder.encode  (compress + thumbnail)
              2. ScreenshotEncryptor.encrypt(bytes, familyId)
                   → fresh AES-GCM content key
                   → KeystoreManager.wrap(contentKey, familyId)
              3. storage.putBytes(ciphertext) → path families/{fid}/screenshots/{devId}/{shotId}.enc
              4. firestore.set(ScreenshotDoc) → families/{fid}/screenshots/{shotId}

6. Child  → requestRepo.updateStatus(UPLOADED) → triggers parent FCM via Cloud Function trigger (or client-side message)

7. Parent ← FCM "SCREENSHOT_RECEIVED"
            ParentFcmService shows "New screenshot received." notification
            (no image bytes, no download URL — only opaque IDs)

8. Parent → open inbox → ScreenshotRepository.fetchDecryptedBitmap(doc)
              1. storage.getBytes(doc.storagePath) → ciphertext
              2. ScreenshotEncryptor.decrypt(payload, familyId)
                   → KeystoreManager.unwrap(wrappedKey, familyId)
                   → content key
                   → AES-GCM decrypt → plaintext bytes
              3. BitmapFactory.decodeByteArray(plaintext) → Bitmap (memory only)

9. Parent → DELETE PERMANENTLY
            ScreenshotRepository.deletePermanently(doc)
              1. storage.delete(families/{fid}/screenshots/{devId}/{shotId}.enc)
              2. firestore.delete(families/{fid}/screenshots/{shotId})
              3. (caller) clear in-memory cache + temp buffer

```

## Pairing flow

```
Parent app                                 Child app

1. PairingIssuer.issueForQr()
   PairingRepository.issuePairingToken()
     firestore.set(pairingTokens/{tokenId}, PairingToken)
   PairingSerializer.encode(token)
     → "PC1:<base64url>"
   QrCodeGenerator.generate(payload, 720)
     → Bitmap
   Render as QR.

                                          2. zxing-android-embedded scanner
                                             decodes the QR
                                             PairingSerializer.decode(scanned)
                                               → PairingToken

                                          3. PairingRepository.redeemPairingToken(token)
                                             firestore.runTransaction { tx ->
                                               stored = tx.get(pairingTokens/{tokenId})
                                               require(!stored.isConsumed)
                                               require(!stored.isExpired)
                                               require(stored.verify(token))
                                               tx.set(token, stored.copy(isConsumed = true, consumedByDeviceId = ...))
                                             }
                                             → returns PairingToken with familyId, parentUserId, etc.

                                          4. DeviceRepository.redeemPairingToken(...)
                                             firestore.set(families/{fid}/devices/{devId}, DeviceDoc)
                                             KeystoreManager.initFamilyKey(fid)
                                             → ParentalCare is now paired.
```

## Data model

```
users/{uid}                          UserDoc { userId, email, displayName, role, familyId, ... }

families/{familyId}                  FamilyDoc { familyId, name, parentUserId, retentionHours, ... }
  ├─ members/{memberId}              MemberDoc { memberId, userId, familyId, role, displayName, ... }
  ├─ devices/{deviceId}              DeviceDoc { deviceId, familyId, ownerMemberId, childDisplayName, fcmToken, isOnline, ... }
  ├─ screenshotRequests/{requestId}  ScreenshotRequestDoc { requestId, familyId, parentUserId, childDeviceId, createdAt, expiresAt, nonce, status, ... }
  ├─ screenshots/{screenshotId}      ScreenshotDoc { screenshotId, familyId, parentUserId, childDeviceId, requestId, storagePath, iv, wrappedKey, retentionExpiresAt, isUnread, ... }
  └─ activityLog/{eventId}           ActivityEvent { eventId, familyId, actorId, actorType, type, message, timestamp }

pairingTokens/{tokenId}              PairingToken { tokenId, opaque, familyId, parentUserId, parentDisplayName, parentEmail, createdAt, expiresAt, nonce, isConsumed, consumedByDeviceId }
```

Storage layout (Cloud Storage for Firebase):

```
families/{familyId}/screenshots/{childDeviceId}/{screenshotId}.enc
```

— ownership-bound, never public, child-write + parent-read + parent-delete enforced by `storage.rules`.

## Notification channels

| Channel ID | Name | Importance | Used for |
|------------|------|------------|----------|
| `channel_monitoring` | Monitoring Status | LOW | Child's persistent "Screen capture service active" |
| `channel_screenshot_request` | Screenshot Requests | HIGH | Child: parent just requested a screenshot |
| `channel_screenshot_received` | Screenshots Received | DEFAULT | Parent: new screenshot arrived |
| `channel_low` | General | LOW | Misc |

## Foreground services

| Service | Type | Lifetime | Purpose |
|---------|------|----------|---------|
| `CaptureForegroundService` | `mediaProjection` | ~5 seconds | Owns the active VirtualDisplay during one capture |
| `MonitoringForegroundService` | `dataSync` | While monitoring enabled | Persistent always-visible indicator (no capture, just the green dot in the status bar) |

The child app starts `CaptureForegroundService` BEFORE constructing `MediaProjection` from the consent result — this is required on Android 14+ (API 34).

## State management

- Compose state hoisted into `androidx.lifecycle.ViewModel` + `StateFlow`.
- Repositories expose suspend methods returning `Result<T>` (typed `AppError`).
- FCM-driven incoming requests are surfaced via `IncomingRequestHandler.active: StateFlow<ScreenshotRequestDoc?>` which the UI collects.
- Hilt provides repositories + managers as singletons scoped to `SingletonComponent`.
- `EncryptedSharedPreferences` ("pc_secure_prefs") is the single backing store for cross-launch local state (e.g. device id, family id, current parent account).

## Build variants

- `debug` — applicationId suffix `.debug`, no R8 minification.
- `release` — R8 minified, resources shrunk, Timber `v/d/i/w` stripped via `assumenosideeffects`.

Both `app-parent` and `app-child` ship debug + release variants.
