package com.example.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.gmail.TokenProvider
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Thrown when no valid Gmail grant exists; the user has to sign in again from the app. */
class AuthRequiredException : IOException("Google sign-in required")

sealed interface AuthResult {
    data class Success(val token: String, val name: String?, val email: String?) : AuthResult
    /** First sign-in or a revoked grant: launch [intentSender] and pass the result to [AuthRepository.completeConsent]. */
    data class NeedsConsent(val intentSender: IntentSender) : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * Google sign-in plus the Gmail grant in one step through the Identity AuthorizationClient.
 * Access tokens are requested on demand and never stored; Play services caches the grant.
 */
class AuthRepository(context: Context) : TokenProvider {
    private val appContext = context.applicationContext
    private val client get() = Identity.getAuthorizationClient(appContext)

    private val request = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(GMAIL_MODIFY), Scope("email"), Scope("profile")))
        .build()

    suspend fun authorize(): AuthResult = suspendCancellableCoroutine { cont ->
        client.authorize(request)
            .addOnSuccessListener { cont.resume(it.toAuthResult()) }
            .addOnFailureListener { cont.resume(AuthResult.Failure(it.message ?: "Authorization failed")) }
    }

    /** Handles the result of the consent screen started from [AuthResult.NeedsConsent]. */
    fun completeConsent(data: Intent?): AuthResult = try {
        client.getAuthorizationResultFromIntent(data).toAuthResult()
    } catch (e: Exception) {
        AuthResult.Failure(e.message ?: "Sign-in cancelled")
    }

    /** Blocking, for the OkHttp interceptor and workers; never call on the main thread. */
    override fun accessToken(): String = runBlocking {
        when (val r = authorize()) {
            is AuthResult.Success -> r.token
            else -> throw AuthRequiredException()
        }
    }

    /** Drops the Google sign-in session. No token is stored here, so there is nothing else to clear. */
    suspend fun signOut() {
        suspendCancellableCoroutine<Unit> { cont ->
            Identity.getSignInClient(appContext).signOut()
                .addOnCompleteListener { cont.resume(Unit) }
        }
    }

    private fun AuthorizationResult.toAuthResult(): AuthResult {
        val pending = pendingIntent
        if (hasResolution() && pending != null) return AuthResult.NeedsConsent(pending.intentSender)
        val token = accessToken ?: return AuthResult.Failure("No access token returned")
        val account = toGoogleSignInAccount()
        return AuthResult.Success(token, account?.displayName, account?.email)
    }

    companion object {
        const val GMAIL_MODIFY = "https://www.googleapis.com/auth/gmail.modify"
    }
}
