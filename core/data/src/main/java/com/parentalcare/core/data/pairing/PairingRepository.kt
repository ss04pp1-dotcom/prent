package com.parentalcare.core.data.pairing

import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.resultOf
import com.parentalcare.core.common.util.Redactor
import com.parentalcare.core.data.model.DeviceDoc
import com.parentalcare.core.security.pairing.PairingToken
import com.parentalcare.core.security.pairing.PairingTokenFactory
import com.parentalcare.core.data.supabase.SupabasePaths
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ConsumePairingArgs(
    val p_token_id: String,
    val p_opaque_raw: String,
    val p_nonce: String,
    val p_device_name: String,
    val p_device_model: String,
    val p_android_version: String,
    val p_fcm_token: String
)

@Singleton
class PairingRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val factory: PairingTokenFactory,
) {
    private val table = SupabasePaths.TABLE_PAIRING_TOKENS

    suspend fun issuePairingToken(
        familyId: String,
        parentDisplayName: String,
        parentEmail: String,
        parentPublicKey: String,
    ): Result<PairingToken> = resultOf {
        val uid = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("not authenticated")
        
        val token = factory.issue(
            familyId = familyId,
            parentUserId = uid,
            parentDisplayName = parentDisplayName,
            parentEmail = parentEmail,
            parentPublicKey = parentPublicKey,
        )
        
        val dbToken = token.copy(opaque = factory.hashKey(token))
        supabase.postgrest[table].insert(dbToken)
        Timber.tag(TAG).i("pairing token issued: tokIdPrefix=%s", Redactor.idPrefix(token.tokenId))
        token
    }

    suspend fun redeemPairingToken(
        token: PairingToken,
        deviceName: String,
        deviceModel: String,
        androidVersion: String,
        fcmToken: String
    ): Result<DeviceDoc> = resultOf {
        val args = ConsumePairingArgs(
            p_token_id = token.tokenId,
            p_opaque_raw = token.opaque,
            p_nonce = token.nonce,
            p_device_name = deviceName,
            p_device_model = deviceModel,
            p_android_version = androidVersion,
            p_fcm_token = fcmToken
        )
        val deviceDoc = supabase.postgrest.rpc("consume_pairing_token", args).decodeAs<DeviceDoc>()
        Timber.tag(TAG).i("pairing token redeemed via RPC: tokIdPrefix=%s", Redactor.idPrefix(token.tokenId))
        deviceDoc
    }

    private companion object { const val TAG = "PC.SupabasePairingRepo" }
}
