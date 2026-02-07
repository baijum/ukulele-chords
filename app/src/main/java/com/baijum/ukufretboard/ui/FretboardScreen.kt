package com.baijum.ukufretboard.ui

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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baijum.ukufretboard.viewmodel.ChordLibraryViewModel
import com.baijum.ukufretboard.viewmodel.FretboardViewModel

/** Tab index for the interactive fretboard explorer. */
private const val TAB_EXPLORER = 0

/** Tab index for the chord library / lookup. */
private const val TAB_LIBRARY = 1

/** Labels displayed in the tab row. */
private val TAB_TITLES = listOf("Explorer", "Chord Library")

/**
 * Top-level screen composable for the Ukulele Chord Explorer.
 *
 * Contains a [PrimaryTabRow] with two tabs:
 * 1. **Explorer**: the interactive fretboard where users tap to select notes
 *    and the app detects the chord.
 * 2. **Chord Library**: a lookup interface where users select a root note and
 *    chord type to browse playable voicings.
 *
 * Both tabs share the same [FretboardViewModel] so that selecting a voicing
 * in the library can load it onto the explorer's fretboard.
 *
 * @param fretboardViewModel The shared [FretboardViewModel] instance.
 * @param libraryViewModel The [ChordLibraryViewModel] for the library tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FretboardScreen(
    fretboardViewModel: FretboardViewModel = viewModel(),
    libraryViewModel: ChordLibraryViewModel = viewModel(),
) {
    var selectedTab by remember { mutableIntStateOf(TAB_EXPLORER) }

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
            // Tab row
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                TAB_EXPLORER -> ExplorerTabContent(viewModel = fretboardViewModel)
                TAB_LIBRARY -> ChordLibraryTab(
                    viewModel = libraryViewModel,
                    onVoicingSelected = { voicing ->
                        fretboardViewModel.applyVoicing(voicing)
                        selectedTab = TAB_EXPLORER
                    },
                )
            }
        }
    }
}

/**
 * Content for the Explorer tab — the original interactive fretboard UI.
 */
@Composable
private fun ExplorerTabContent(viewModel: FretboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
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
