import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { importPKCS8, SignJWT } from "https://deno.land/x/jose@v4.14.4/index.ts";

const FIREBASE_PROJECT_ID = "parentcare-f7f62";
const FIREBASE_CLIENT_EMAIL = "firebase-adminsdk-fbsvc@parentcare-f7f62.iam.gserviceaccount.com";
const FIREBASE_PRIVATE_KEY = `-----BEGIN PRIVATE KEY-----
MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDLTSzhAUuyuJZ6
EH5NPJWUaL3cMNJHii87e4mYdndOUfqtW6YTJOQFfE75ehO1gEZ2T5WjsskBqL3f
Biv2AFMVRuE4Bklb58RY9P+zbuSD2B2l4JICLAzPGbqxYNXt0YuelXY9phlezrOM
4bVO8BO4ToPuyzhfjC97o2wFg70NKmV3uywR2CASHdGeGICR5cO6e3wgNY4LIu/L
8Yf3qGG7tp0/5gNsQU7drmvPhNPWt51BFk2Fz9CMNPTbL3WXpPtnL+9/Sw7prP8a
3Zhm/XwyWosSlIB0YjxxSjCDi4ZEIy1evm6YHsLEbVHfaOFg5KacZr6rVTDXoAuA
l19NGUtpAgMBAAECggEAEN5Nrbqzivf1ZlnlfOecuqRpMnKrk+uDiUIyE8AqnIlB
TS6ITFjYgZsDm4AVUA89aXYngp5kPrq/TFjA/4ed3StVJxagcht8J2xmpf76v3TI
WvnT65G3m1x1aN/RyEqFdOpOVgWo37yzWaplpo/VdwquuDrM6eKiEvZnkVqRrlyT
B7mby1rd9eRv6277AgvAbi4GQFb2bq0RJ/ftS0Oh6KoEnXf08jJfAFNjMb64ovod
spkknGnJe/Fri8ZldhSNGwi6yIOu0XEIFN3Esy4IQfMQYTgL3fguqS1wuXENAI+x
6auwsUwXHDAWT9Wg9K4SvH33fL634NhdoHRdACsGIQKBgQDwKvwMp9Rv9rGnVowS
9Lw07LZpfmbDpdiFN5eSZM/E6ZX6ls4zorgh6BUdeoefQOjKqIdU4ZX94kzCKi1F
UrUDlaB+vIC1A1ommk2XyTVJDrjeDWJydPmHlAMGEx3+Bt1s0UaaWdQ45qz/xbjs
dq5s5S4V0YwbwGxexHWDJk+uGQKBgQDYtAqlq96NRMwhn4fjS/CnLoqXz6nqXfhU
2efaSGIt0dw3rPxAWB7+M4wfhKXnfzXPWWYP6EVEjApUjhpLFNurJceE8i0kHmCd
coFDh1+kTfOZol6xT8Hv8UUNR5wbZpwVTMQRKcl6TjZGf82OJuf5NyKgvkg2JfwW
n1gntfCR0QKBgQDe07LY9xnl66e2j1kU+VZpcDpqNGmaGpSCPSr3G2ud0e9h+WtF
gzJ92vtER3vvaOTTqnstaMOmM8Ft4H3yNKNXuQqYq8Jqr7qD2GQncPpsVyl0dEyj
zx35CX2otDH+j1X/cUelS7TYOkXGZSeG1TJOPHB+s9Xfpa+mWXaCARiqeQKBgQCA
hE4oMMS8/UcezbDWYUwoqhZxsCuyHebP06eIMhNf3yz+K+4x7tD6qvF4I8AMicoM
3ZUE7BhgbbkWqmv3iTg5yR8JvmJ9IDLmTWlR9jAHjl9hBwWnnIiE146/MG/pZDI/
A5boIuMlUMsphlQO8Q30I/m6YP8MdfyKIhgy/NjecQKBgQDclhpDaKRiAUWjcoyz
yMJPXSGs9iwrGmh7ybFRUu92xN10o7Ay2VPGdyuEPFR+5jLUwTbA4p6FJwgNuFG7
TxhK5p1uNmKJdWzxTtz5IwwQMbInHPZRLCWVXovqvqK5i6Wdge48j3QNJswRF7tY
pHbdBYwOrpboW5kHG2daBRSAEA==
-----END PRIVATE KEY-----`;

async function getAccessToken() {
  const privateKey = await importPKCS8(FIREBASE_PRIVATE_KEY, "RS256");
  const jwt = await new SignJWT({
    iss: FIREBASE_CLIENT_EMAIL,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
  })
    .setProtectedHeader({ alg: "RS256", typ: "JWT" })
    .setIssuedAt()
    .setExpirationTime("1h")
    .sign(privateKey);

  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });

  const data = await response.json();
  if (!response.ok) throw new Error("Failed to get access token");
  return data.access_token;
}

serve(async (req) => {
  try {
    const payload = await req.json();
    const record = payload.record;

    if (!record || !record.child_device_id || record.status !== 'REQUESTED') {
      return new Response(JSON.stringify({ message: "Ignored, not a new request" }), { status: 200 });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") || "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    const { data: deviceData, error: deviceError } = await supabase
      .from('devices')
      .select('fcm_token')
      .eq('device_id', record.child_device_id)
      .single();

    if (deviceError || !deviceData || !deviceData.fcm_token) {
      console.error("Device/FCM token not found:", deviceError);
      return new Response(JSON.stringify({ error: "FCM token not found" }), { status: 400 });
    }

    const fcmToken = deviceData.fcm_token;
    const accessToken = await getAccessToken();

    const fcmResponse = await fetch(`https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token: fcmToken,
          data: {
            type: "SCREENSHOT_REQUEST",
            requestId: record.request_id,
            familyId: record.family_id
          },
          android: {
            priority: "high",
            direct_boot_ok: true
          }
        }
      })
    });

    const fcmResult = await fcmResponse.json();
    
    if (!fcmResponse.ok) {
      console.error("FCM Send Error:", fcmResult);
      return new Response(JSON.stringify({ error: "FCM Send Error", details: fcmResult }), { status: 500 });
    }

    return new Response(JSON.stringify({ success: true }), { status: 200 });
  } catch (error) {
    console.error("Internal Error:", error);
    return new Response(JSON.stringify({ error: error.message }), { status: 500 });
  }
});
