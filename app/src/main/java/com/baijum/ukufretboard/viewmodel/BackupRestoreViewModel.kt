package com.baijum.ukufretboard.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baijum.ukufretboard.data.sync.BackupRestoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * State of a backup or restore operation.
 */
sealed class BackupRestoreState {
    /** No operation in progress. */
    data object Idle : BackupRestoreState()

    /** A backup export is in progress. */
    data object Exporting : BackupRestoreState()

    /** A backup import is in progress. */
    data object Importing : BackupRestoreState()

    /** The last operation completed successfully. */
    data class Success(val message: String) : BackupRestoreState()

    /** The last operation failed. */
    data class Error(val message: String) : BackupRestoreState()
}

/**
 * UI state for the Backup & Restore section.
 */
data class BackupRestoreUiState(
    /** Current operation state. */
    val state: BackupRestoreState = BackupRestoreState.Idle,

    /** Formatted date of the last successful backup, or null. */
    val lastBackupDate: String? = null,
)

/**
 * ViewModel for the Backup & Restore feature.
 *
 * Uses Android's Storage Access Framework (SAF) to read/write backup files
 * via content URIs — no runtime permissions required. The user picks the
 * file location through the system file picker.
 *
 * Requires a [SettingsViewModel] reference for exporting/importing settings.
 * Call [init] before using [exportBackup] or [importBackup].
 */
class BackupRestoreViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(
        PREFS_NAME, android.content.Context.MODE_PRIVATE,
    )

    private val _uiState = MutableStateFlow(
        BackupRestoreUiState(lastBackupDate = loadLastBackupDate())
    )
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    private var manager: BackupRestoreManager? = null

    /**
     * Initialises the manager with the required [SettingsViewModel].
     * Must be called before export or import.
     */
    fun init(settingsViewModel: SettingsViewModel) {
        if (manager == null) {
            manager = BackupRestoreManager(
                context = getApplication(),
                settingsViewModel = settingsViewModel,
            )
        }
    }

    /**
     * Exports all user data to the file at [uri] via SAF.
     */
    fun exportBackup(uri: Uri) {
        val mgr = manager ?: return
        _uiState.update { it.copy(state = BackupRestoreState.Exporting) }

        viewModelScope.launch {
            try {
                val jsonContent = withContext(Dispatchers.IO) {
                    mgr.exportBackup()
                }

                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver
                        .openOutputStream(uri)?.use { stream ->
                            stream.write(jsonContent.toByteArray(Charsets.UTF_8))
                        } ?: throw Exception("Could not open file for writing")
                }

                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_LAST_BACKUP, now).apply()
                val dateStr = formatDate(now)

                _uiState.update {
                    it.copy(
                        state = BackupRestoreState.Success("Backup saved successfully"),
                        lastBackupDate = dateStr,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(state = BackupRestoreState.Error(
                        "Backup failed: ${e.message ?: "Unknown error"}"
                    ))
                }
            }
        }
    }

    /**
     * Imports user data from the backup file at [uri] via SAF.
     */
    fun importBackup(uri: Uri) {
        val mgr = manager ?: return
        _uiState.update { it.copy(state = BackupRestoreState.Importing) }

        viewModelScope.launch {
            try {
                val jsonContent = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver
                        .openInputStream(uri)?.use { stream ->
                            stream.bufferedReader(Charsets.UTF_8).readText()
                        } ?: throw Exception("Could not open file for reading")
                }

                withContext(Dispatchers.IO) {
                    mgr.importBackup(jsonContent)
                }

                _uiState.update {
                    it.copy(state = BackupRestoreState.Success("Data restored successfully"))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(state = BackupRestoreState.Error(
                        "Restore failed: ${e.message ?: "Unknown error"}"
                    ))
                }
            }
        }
    }

    /**
     * Resets the operation state back to idle (e.g. after the user dismisses
     * a success/error message).
     */
    fun resetState() {
        _uiState.update { it.copy(state = BackupRestoreState.Idle) }
    }

    private fun loadLastBackupDate(): String? {
        val millis = prefs.getLong(KEY_LAST_BACKUP, 0L)
        return if (millis > 0L) formatDate(millis) else null
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            .format(Date(millis))

    companion object {
        private const val PREFS_NAME = "backup_metadata"
        private const val KEY_LAST_BACKUP = "last_backup_at"
    }
}
