package cc.toolcrate.lyceum.backup.drive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface DriveAccess {
    data class Granted(val token: String) : DriveAccess
    /** Google needs to show its account picker or consent screen first. */
    data class NeedsUser(val intent: IntentSender) : DriveAccess
    data class Failed(val reason: String) : DriveAccess
}

/**
 * Asks Google for access to Lyceum's hidden app folder in the user's Drive, and nothing else.
 * There is no Lyceum account: Google's own authorization is all there is.
 */
@Singleton
class DriveAuth @Inject constructor(@ApplicationContext private val context: Context) {
    private val request = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(DriveClient.SCOPE)))
        .build()

    /** From an activity: may hand back an [IntentSender] for Google's screen. */
    suspend fun authorize(activity: Activity): DriveAccess = authorizeWith(activity)

    /** From the background: works once the user has granted access before. */
    suspend fun authorizeSilently(): DriveAccess = when (val r = authorizeWith(context)) {
        is DriveAccess.NeedsUser -> DriveAccess.Failed("Google Drive needs you to reconnect. Open Settings → Backup.")
        else -> r
    }

    fun resultFromIntent(data: Intent?): DriveAccess = try {
        val result = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(data)
        result.accessToken?.let { DriveAccess.Granted(it) } ?: DriveAccess.Failed(NOT_SET_UP)
    } catch (e: ApiException) {
        DriveAccess.Failed(if (e.statusCode == CommonStatusCodes.CANCELED) "Google Drive wasn't connected." else describe(e))
    }

    private suspend fun authorizeWith(ctx: Context): DriveAccess = try {
        val result = Identity.getAuthorizationClient(ctx).authorize(request).await()
        when {
            result.hasResolution() -> result.pendingIntent?.intentSender?.let { DriveAccess.NeedsUser(it) }
                ?: DriveAccess.Failed(NOT_SET_UP)
            result.accessToken != null -> DriveAccess.Granted(result.accessToken!!)
            else -> DriveAccess.Failed(NOT_SET_UP)
        }
    } catch (e: ApiException) {
        DriveAccess.Failed(describe(e))
    } catch (e: Exception) {
        DriveAccess.Failed("Google Drive couldn't be reached. Check the connection and try again.")
    }

    private fun describe(e: ApiException): String = when (e.statusCode) {
        CommonStatusCodes.NETWORK_ERROR -> "Google Drive couldn't be reached. Check the connection and try again."
        CommonStatusCodes.DEVELOPER_ERROR -> NOT_SET_UP
        else -> "Google Drive didn't connect (code ${e.statusCode})."
    }

    private companion object {
        const val NOT_SET_UP = "Google sign-in isn't set up for this copy of Lyceum yet, so Drive backup can't connect."
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
