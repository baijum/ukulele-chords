package com.example.ukufretboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ukufretboard.viewmodel.FretboardViewModel

/**
 * Top-level screen composable for the Ukulele Chord Explorer.
 *
 * Composes the full UI from three main sections:
 * 1. **Top bar**: app title
 * 2. **Fretboard**: interactive grid for selecting notes
 * 3. **Action buttons**: Reset and Toggle Notes controls
 * 4. **Chord result**: detected chord name, quality, and notes
 *
 * Observes the [FretboardViewModel] state via [collectAsState] and passes
 * it down to child composables. All user interactions are delegated to
 * the ViewModel.
 *
 * @param viewModel The [FretboardViewModel] instance (provided by default via [viewModel]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FretboardScreen(
    viewModel: FretboardViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ukulele Chord Explorer",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Interactive fretboard
            FretboardView(
                tuning = viewModel.tuning,
                selections = uiState.selections,
                showNoteNames = uiState.showNoteNames,
                onFretTap = viewModel::toggleFret,
                getNoteAt = viewModel::getNoteAt,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
            )

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 12.dp,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                OutlinedButton(onClick = viewModel::clearAll) {
                    Text("Reset")
                }
                OutlinedButton(onClick = viewModel::toggleNoteNames) {
                    Text(if (uiState.showNoteNames) "Hide Notes" else "Show Notes")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chord detection result
            ChordResultView(
                detectionResult = uiState.detectionResult,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
