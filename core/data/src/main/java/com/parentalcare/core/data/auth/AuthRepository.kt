package com.parentalcare.core.data.auth

import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.resultOf
import com.parentalcare.core.common.util.Redactor
import com.parentalcare.core.data.model.UserDoc
import com.parentalcare.core.data.model.FamilyDoc
import com.parentalcare.core.data.supabase.SupabasePaths
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val usersTable = SupabasePaths.TABLE_USERS

    val currentUser get() = supabase.auth.currentUserOrNull()

    suspend fun signUpParent(
        email: String,
        password: String,
        displayName: String,
    ): Result<UserDoc> = resultOf {
        val user = supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val uid = user?.id ?: throw IllegalStateException("no user after sign up")

        val userDoc = UserDoc(
            userId = uid,
            email = email,
            displayName = displayName,
            role = "PARENT",
        )
        
        try {
            supabase.postgrest[usersTable].insert(userDoc)
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert user doc. This might happen if Email Confirmation is required but not done.")
            // We can ignore this error for now, because the user is created in Auth. 
            // They just won't have a profile doc until they sign in.
            // But wait, if they aren't logged in, they can't continue anyway!
            // If they are not logged in, we should throw an exception telling them to verify email.
            if (supabase.auth.currentUserOrNull() == null) {
                throw Exception("Account created! Please check your email to verify your account, then log in.")
            }
        }
        
        Timber.tag(TAG).i("parent signup: uidPrefix=%s", Redactor.idPrefix(uid))
        userDoc
    }

    suspend fun signInParent(email: String, password: String): Result<UserDoc> = resultOf {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = supabase.auth.currentUserOrNull() ?: throw IllegalStateException("no user after sign in")
        val uid = user.id

        val userDoc = supabase.postgrest[usersTable]
            .select { filter { eq(SupabasePaths.Columns.USER_ID, uid) } }
            .decodeSingleOrNull<UserDoc>()
            ?: UserDoc(userId = uid, email = email, displayName = email, role = "PARENT")

        Timber.tag(TAG).i("parent signin: uidPrefix=%s", Redactor.idPrefix(uid))
        userDoc
    }

    suspend fun signInAnonymously(): Result<Unit> = resultOf {
        supabase.auth.signInAnonymously()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = resultOf {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    suspend fun signOut() = supabase.auth.signOut()

    suspend fun signInWithGoogle(idToken: String): Result<UserDoc> = resultOf {
        supabase.auth.signInWith(IDToken) {
            provider = io.github.jan.supabase.gotrue.providers.Google
            this.idToken = idToken
        }
        val user = supabase.auth.currentUserOrNull() ?: throw IllegalStateException("no user after google sign in")
        val uid = user.id
        val email = user.email ?: ""
        val displayName = user.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"") ?: email

        var userDoc = supabase.postgrest[usersTable]
            .select { filter { eq(SupabasePaths.Columns.USER_ID, uid) } }
            .decodeSingleOrNull<UserDoc>()

        if (userDoc == null) {
            userDoc = UserDoc(userId = uid, email = email, displayName = displayName, role = "PARENT")
            try {
            supabase.postgrest[usersTable].insert(userDoc)
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert user doc. This might happen if Email Confirmation is required but not done.")
            // We can ignore this error for now, because the user is created in Auth. 
            // They just won't have a profile doc until they sign in.
            // But wait, if they aren't logged in, they can't continue anyway!
            // If they are not logged in, we should throw an exception telling them to verify email.
            if (supabase.auth.currentUserOrNull() == null) {
                throw Exception("Account created! Please check your email to verify your account, then log in.")
            }
        }
        }

        Timber.tag(TAG).i("parent google signin: uidPrefix=%s", Redactor.idPrefix(uid))
        userDoc
    }

    suspend fun getFamilyForUser(userId: String): Result<FamilyDoc> = resultOf {
        supabase.postgrest[SupabasePaths.TABLE_FAMILIES]
            .select { filter { eq(SupabasePaths.Columns.PARENT_USER_ID, userId) } }
            .decodeSingleOrNull<FamilyDoc>()
            ?: throw IllegalStateException("No family found for user")
    }

    private companion object { const val TAG = "PC.SupabaseAuthRepo" }
}
