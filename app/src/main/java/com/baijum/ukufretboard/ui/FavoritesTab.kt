package com.baijum.ukufretboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.data.FavoriteVoicing
import com.baijum.ukufretboard.data.Notes
import com.baijum.ukufretboard.domain.ChordVoicing
import com.baijum.ukufretboard.viewmodel.FavoritesViewModel

private const val FILTER_ALL = "__all__"
private const val FILTER_UNFILED = "__unfiled__"

/**
 * Tab displaying the user's saved favorite chord voicings, organized by folders.
 *
 * Folder chips at the top filter the grid: All, Unfiled, and user-created folders.
 */
@Composable
fun FavoritesTab(
    viewModel: FavoritesViewModel,
    onVoicingSelected: (ChordVoicing) -> Unit,
    useFlats: Boolean = false,
    leftHanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val favorites by viewModel.favorites.collectAsState()
    val folders by viewModel.folders.collectAsState()
    var selectedFilter by remember { mutableStateOf(FILTER_ALL) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf<FavoriteVoicing?>(null) }

    if (favorites.isEmpty()) {
        // Empty state
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "No favorites yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Browse the Chords tab and tap the heart icon on a voicing to save it here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            // Folder chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == FILTER_ALL,
                        onClick = { selectedFilter = FILTER_ALL },
                        label = { Text("All (${favorites.size})") },
                    )
                }
                item {
                    val unfiledCount = favorites.count { it.folderId == null }
                    FilterChip(
                        selected = selectedFilter == FILTER_UNFILED,
                        onClick = { selectedFilter = FILTER_UNFILED },
                        label = { Text("Unfiled ($unfiledCount)") },
                    )
                }
                items(folders) { folder ->
                    val count = favorites.count { it.folderId == folder.id }
                    FilterChip(
                        selected = selectedFilter == folder.id,
                        onClick = { selectedFilter = folder.id },
                        label = { Text("${folder.name} ($count)") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    viewModel.deleteFolder(folder.id)
                                    if (selectedFilter == folder.id) selectedFilter = FILTER_ALL
                                },
                                modifier = Modifier.size(18.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete folder",
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        },
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { showCreateFolderDialog = true },
                        label = { Text("+") },
                    )
                }
            }

            // Filtered grid
            val filtered = when (selectedFilter) {
                FILTER_ALL -> favorites
                FILTER_UNFILED -> favorites.filter { it.folderId == null }
                else -> favorites.filter { it.folderId == selectedFilter }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filtered) { favorite ->
                    val voicing = viewModel.toChordVoicing(favorite, useFlats)
                    val chordName = Notes.pitchClassToName(favorite.rootPitchClass, useFlats) + favorite.chordSymbol

                    Box {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = chordName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            ChordDiagramPreview(
                                voicing = voicing,
                                onClick = { onVoicingSelected(voicing) },
                                leftHanded = leftHanded,
                            )
                        }
                        // Actions: remove + move to folder
                        Row(modifier = Modifier.align(Alignment.TopEnd)) {
                            if (folders.isNotEmpty()) {
                                IconButton(
                                    onClick = { showMoveDialog = favorite },
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Move to folder",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.removeFavorite(favorite) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Remove from favorites",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create folder dialog
    if (showCreateFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            viewModel.createFolder(folderName.trim())
                            showCreateFolderDialog = false
                        }
                    },
                    enabled = folderName.isNotBlank(),
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Move to folder dialog
    showMoveDialog?.let { favorite ->
        AlertDialog(
            onDismissRequest = { showMoveDialog = null },
            title = { Text("Move to Folder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        viewModel.moveToFolder(favorite, null)
                        showMoveDialog = null
                    }) { Text("Unfiled") }
                    folders.forEach { folder ->
                        TextButton(onClick = {
                            viewModel.moveToFolder(favorite, folder.id)
                            showMoveDialog = null
                        }) { Text(folder.name) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveDialog = null }) { Text("Cancel") }
            },
        )
    }
}
