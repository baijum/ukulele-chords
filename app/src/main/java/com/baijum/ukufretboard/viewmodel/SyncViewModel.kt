package com.baijum.ukufretboard.viewmodel

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baijum.ukufretboard.data.ChordSheetRepository
import com.baijum.ukufretboard.data.FavoritesRepository
import com.baijum.ukufretboard.data.sync.GoogleDriveRepository
import com.baijum.ukufretboard.data.sync.GoogleSignInHelper
import com.baijum.ukufretboard.data.sync.SyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Possible states for the sync UI.
 */
sealed class SyncState {
    /** User is not signed in to Google. */
    data object SignedOut : SyncState()

    /** User is signed in but no sync in progress. */
    data class Idle(
        val email: String,
        val lastSyncedAt: Long? = null,
    ) : SyncState()

    /** Sync is currently in progress. */
    data class Syncing(val email: String) : SyncState()

    /** Sync completed successfully. */
    data class Success(
        val email: String,
        val syncedAt: Long,
    ) : SyncState()

    /** Sync failed. */
    data class Error(
        val email: String,
        val message: String,
    ) : SyncState()
}

/**
 * ViewModel for managing Google Drive sync operations.
 *
 * Depends on [SettingsViewModel] for reading/writing app settings
 * during sync. The [SettingsViewModel] must be provided via [init].
 */
class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    val signInHelper = GoogleSignInHelper(context)
    private val driveRepository = GoogleDriveRepository(context)
    private val favoritesRepository = FavoritesRepository(context)
    private val chordSheetRepository = ChordSheetRepository(context)

    private val syncPrefs: SharedPreferences =
        context.getSharedPreferences("sync_metadata", android.content.Context.MODE_PRIVATE)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.SignedOut)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var settingsViewModel: SettingsViewModel? = null

    /**
     * Provides the [SettingsViewModel] reference needed for sync.
     * Must be called before [performSync].
     */
    fun init(settingsVm: SettingsViewModel) {
        settingsViewModel = settingsVm
        refreshSignInState()
    }

    /**
     * Re-checks the current Google Sign-In state and updates [syncState].
     */
    fun refreshSignInState() {
        val account = signInHelper.getSignedInAccount()
        _syncState.value = if (account != null) {
            SyncState.Idle(
                email = account.email ?: "",
                lastSyncedAt = syncPrefs.getLong(KEY_LAST_SYNCED, 0L).takeIf { it > 0 },
            )
        } else {
            SyncState.SignedOut
        }
    }

    /**
     * Handles the result of the Google Sign-In intent.
     */
    fun handleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                _syncState.value = SyncState.Idle(
                    email = account.email ?: "",
                    lastSyncedAt = syncPrefs.getLong(KEY_LAST_SYNCED, 0L).takeIf { it > 0 },
                )
            }
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(
                email = "",
                message = e.localizedMessage ?: "Sign-in failed",
            )
        }
    }

    /**
     * Signs out and resets state.
     */
    fun signOut() {
        signInHelper.signOut {
            _syncState.value = SyncState.SignedOut
        }
    }

    /**
     * Performs the full sync operation in a background coroutine.
     */
    fun performSync() {
        val account = signInHelper.getSignedInAccount() ?: return
        val settingsVm = settingsViewModel ?: return

        val email = account.email ?: ""
        _syncState.value = SyncState.Syncing(email)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val syncManager = SyncManager(
                    favoritesRepository = favoritesRepository,
                    chordSheetRepository = chordSheetRepository,
                    driveRepository = driveRepository,
                    getSettings = { settingsVm.exportSettings() },
                    setSettings = { newSettings -> settingsVm.replaceAll(newSettings) },
                )
                syncManager.sync(account)

                val now = System.currentTimeMillis()
                syncPrefs.edit().putLong(KEY_LAST_SYNCED, now).apply()

                _syncState.value = SyncState.Success(email = email, syncedAt = now)
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(
                    email = email,
                    message = e.localizedMessage ?: "Sync failed",
                )
            }
        }
    }

    companion object {
        private const val KEY_LAST_SYNCED = "last_synced_at"
    }
}
