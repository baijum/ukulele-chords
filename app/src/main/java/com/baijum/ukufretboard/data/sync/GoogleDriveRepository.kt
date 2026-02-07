package com.baijum.ukufretboard.data.sync

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.ByteArrayOutputStream

/**
 * Handles uploading and downloading the backup JSON file to/from
 * Google Drive's `appDataFolder`.
 *
 * The `appDataFolder` is a hidden, app-specific folder that the user
 * never sees in their Drive. Only this app can access it.
 */
class GoogleDriveRepository(
    private val context: android.content.Context,
) {

    /**
     * Uploads the given JSON string to Drive, overwriting any existing backup.
     *
     * If a backup file already exists, it is updated. Otherwise, a new file
     * is created in the `appDataFolder`.
     *
     * @param account The signed-in Google account.
     * @param jsonContent The JSON backup content.
     */
    fun upload(account: GoogleSignInAccount, jsonContent: String) {
        val drive = buildDriveService(account)
        val existingFileId = findBackupFileId(drive)

        val mediaContent = ByteArrayContent.fromString("application/json", jsonContent)

        if (existingFileId != null) {
            // Update existing file
            drive.files().update(existingFileId, null, mediaContent).execute()
        } else {
            // Create new file in appDataFolder
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = BACKUP_FILE_NAME
                parents = listOf("appDataFolder")
            }
            drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
        }
    }

    /**
     * Downloads the backup JSON from Drive, or null if no backup exists.
     *
     * @param account The signed-in Google account.
     * @return The JSON string, or null if no backup file was found.
     */
    fun download(account: GoogleSignInAccount): String? {
        val drive = buildDriveService(account)
        val fileId = findBackupFileId(drive) ?: return null

        val outputStream = ByteArrayOutputStream()
        drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        return outputStream.toString("UTF-8")
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private fun buildDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA),
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        )
            .setApplicationName("UkuleleChordExplorer")
            .build()
    }

    private fun findBackupFileId(drive: Drive): String? {
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE_NAME'")
            .setFields("files(id)")
            .setPageSize(1)
            .execute()

        return result.files?.firstOrNull()?.id
    }

    companion object {
        private const val BACKUP_FILE_NAME = "ukulele_chords_backup.json"
    }
}
