package com.baijum.ukufretboard.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.viewmodel.SyncState
import com.baijum.ukufretboard.viewmodel.SyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sync section displayed at the bottom of the Settings sheet.
 *
 * Shows sign-in/sign-out state, a "Sync Now" button, and sync status.
 */
@Composable
fun SyncSection(
    syncViewModel: SyncViewModel,
) {
    val syncState by syncViewModel.syncState.collectAsState()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            syncViewModel.handleSignInResult(result.data)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section header
        Text(
            text = "GOOGLE DRIVE SYNC",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        when (val state = syncState) {
            is SyncState.SignedOut -> {
                Text(
                    text = "Sign in to back up and sync your data across devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = syncViewModel.signInHelper.getSignInIntent()
                        signInLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Sign in with Google")
                }
            }

            is SyncState.Idle -> {
                AccountRow(email = state.email)
                state.lastSyncedAt?.let { timestamp ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Last synced: ${formatTimestamp(timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                SyncActionRow(
                    onSync = { syncViewModel.performSync() },
                    onSignOut = { syncViewModel.signOut() },
                )
            }

            is SyncState.Syncing -> {
                AccountRow(email = state.email)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Syncing...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            is SyncState.Success -> {
                AccountRow(email = state.email)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last synced: ${formatTimestamp(state.syncedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                SyncActionRow(
                    onSync = { syncViewModel.performSync() },
                    onSignOut = { syncViewModel.signOut() },
                )
            }

            is SyncState.Error -> {
                AccountRow(email = state.email)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sync error: ${state.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(12.dp))
                SyncActionRow(
                    onSync = { syncViewModel.performSync() },
                    onSignOut = { syncViewModel.signOut() },
                )
            }
        }
    }
}

@Composable
private fun AccountRow(email: String) {
    Text(
        text = "Signed in as $email",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SyncActionRow(
    onSync: () -> Unit,
    onSignOut: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onSync,
            modifier = Modifier.weight(1f),
        ) {
            Text("Sync Now")
        }
        OutlinedButton(
            onClick = onSignOut,
        ) {
            Text("Sign Out")
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}
