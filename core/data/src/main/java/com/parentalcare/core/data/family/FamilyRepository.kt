package com.parentalcare.core.data.family

import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.resultOf
import com.parentalcare.core.common.util.Redactor
import com.parentalcare.core.data.model.FamilyDoc
import com.parentalcare.core.data.model.UserDoc
import com.parentalcare.core.data.supabase.SupabasePaths
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val familiesTable = SupabasePaths.TABLE_FAMILIES
    private val usersTable = SupabasePaths.TABLE_USERS

    suspend fun getOrCreateFamily(): Result<FamilyDoc> = resultOf {
        val user = supabase.auth.currentUserOrNull() ?: throw IllegalStateException("Not authenticated")
        val uid = user.id

        val existing = supabase.postgrest[familiesTable]
            .select { filter { eq(SupabasePaths.Columns.PARENT_USER_ID, uid) } }
            .decodeSingleOrNull<FamilyDoc>()

        if (existing != null) {
            return@resultOf existing
        }

        val displayName = user.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"") ?: "Parent"
        val familyId = UUID.randomUUID().toString()
        val newFamily = FamilyDoc(
            familyId = familyId,
            name = "$displayName's Family",
            parentUserId = uid,
        )

        supabase.postgrest[familiesTable].insert(newFamily)

        try {
            supabase.postgrest[usersTable].update(
                mapOf(SupabasePaths.Columns.FAMILY_ID to familyId)
            ) {
                filter { eq(SupabasePaths.Columns.USER_ID, uid) }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w("Failed to update user with familyId: %s", Redactor.redact(e.message))
        }

        newFamily
    }

    suspend fun updateFamily(family: FamilyDoc): Result<Unit> = resultOf {
        supabase.postgrest[familiesTable].update(family) {
            filter { eq(SupabasePaths.Columns.FAMILY_ID, family.familyId) }
        }
    }

    private companion object { const val TAG = "PC.SupabaseFamilyRepo" }
}
