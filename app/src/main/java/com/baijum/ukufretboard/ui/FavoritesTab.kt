package com.baijum.ukufretboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.data.FavoriteVoicing
import com.baijum.ukufretboard.data.Notes
import com.baijum.ukufretboard.domain.ChordVoicing
import com.baijum.ukufretboard.viewmodel.FavoritesViewModel

/**
 * Tab displaying the user's saved favorite chord voicings.
 *
 * Shows a grid of chord diagrams that can be tapped to apply to the fretboard.
 * Displays an empty state message when no favorites are saved.
 *
 * @param viewModel The [FavoritesViewModel] managing favorites.
 * @param onVoicingSelected Callback when a favorite voicing is tapped.
 * @param useFlats Whether to use flat note names.
 * @param leftHanded Whether to render diagrams in left-handed mode.
 * @param modifier Optional modifier.
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
                text = "Browse the Chords tab and long-press a voicing to save it here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(favorites) { favorite ->
                val voicing = viewModel.toChordVoicing(favorite, useFlats)
                val chordName = Notes.pitchClassToName(favorite.rootPitchClass, useFlats) + favorite.chordSymbol

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
            }
        }
    }
}
