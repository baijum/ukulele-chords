package com.baijum.ukufretboard.data.sync

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

/**
 * Wraps Google Sign-In configuration for Drive appDataFolder access.
 *
 * Only requests the `drive.appdata` scope, which limits access to a
 * hidden, app-specific folder on the user's Drive — no broad file access.
 */
class GoogleSignInHelper(private val context: Context) {

    private val signInOptions: GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

    private val client: GoogleSignInClient =
        GoogleSignIn.getClient(context, signInOptions)

    /**
     * Returns the currently signed-in account, or null if not signed in
     * or if the required Drive scope has not been granted.
     */
    fun getSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null &&
            GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))
        ) {
            account
        } else {
            null
        }
    }

    /**
     * Returns an [Intent] to launch the Google Sign-In flow.
     * Use with `ActivityResultLauncher` or `startActivityForResult`.
     */
    fun getSignInIntent(): Intent = client.signInIntent

    /**
     * Signs out the current user.
     */
    fun signOut(onComplete: () -> Unit = {}) {
        client.signOut().addOnCompleteListener { onComplete() }
    }
}
