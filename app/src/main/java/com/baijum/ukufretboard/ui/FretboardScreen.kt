package com.baijum.ukufretboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baijum.ukufretboard.viewmodel.ChordLibraryViewModel
import com.baijum.ukufretboard.viewmodel.FavoritesViewModel
import com.baijum.ukufretboard.viewmodel.FretboardViewModel
import com.baijum.ukufretboard.viewmodel.SettingsViewModel
import com.baijum.ukufretboard.viewmodel.SongbookViewModel

/** Tab index for the interactive fretboard explorer. */
private const val TAB_EXPLORER = 0

/** Tab index for the chord library / lookup. */
private const val TAB_LIBRARY = 1

/** Tab index for strumming patterns reference. */
private const val TAB_PATTERNS = 2

/** Tab index for chord progressions. */
private const val TAB_PROGRESSIONS = 3

/** Tab index for saved favorites. */
private const val TAB_FAVORITES = 4

/** Tab index for songbook / chord sheets. */
private const val TAB_SONGBOOK = 5

/** Labels displayed in the tab row. */
private val TAB_TITLES = listOf("Explorer", "Chords", "Patterns", "Progressions", "Favorites", "Songs")

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
 * @param settingsViewModel The shared [SettingsViewModel] for app-wide settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FretboardScreen(
    fretboardViewModel: FretboardViewModel = viewModel(),
    libraryViewModel: ChordLibraryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel(),
    songbookViewModel: SongbookViewModel = viewModel(),
) {
    var selectedTab by remember { mutableIntStateOf(TAB_EXPLORER) }
    var showSettings by remember { mutableStateOf(false) }

    val appSettings by settingsViewModel.settings.collectAsState()

    // Keep FretboardViewModel in sync with sound settings
    LaunchedEffect(appSettings.sound) {
        fretboardViewModel.setSoundSettings(appSettings.sound)
    }

    // Sync useFlats preference
    LaunchedEffect(appSettings.display.useFlats) {
        fretboardViewModel.setUseFlats(appSettings.display.useFlats)
        libraryViewModel.setUseFlats(appSettings.display.useFlats)
    }

    // Sync tuning settings
    LaunchedEffect(appSettings.tuning) {
        fretboardViewModel.setTuningSettings(appSettings.tuning)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ukulele Chord Explorer",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
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
            // Tab row (scrollable for 6 tabs)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp,
            ) {
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
                TAB_EXPLORER -> ExplorerTabContent(
                    viewModel = fretboardViewModel,
                    soundEnabled = appSettings.sound.enabled,
                    leftHanded = appSettings.fretboard.leftHanded,
                    useFlats = appSettings.display.useFlats,
                )
                TAB_LIBRARY -> ChordLibraryTab(
                    viewModel = libraryViewModel,
                    onVoicingSelected = { voicing ->
                        fretboardViewModel.applyVoicing(voicing)
                        selectedTab = TAB_EXPLORER
                    },
                    onVoicingLongPressed = { voicing ->
                        val state = libraryViewModel.uiState.value
                        val symbol = state.selectedFormula?.symbol ?: ""
                        favoritesViewModel.toggleFavorite(
                            rootPitchClass = state.selectedRoot,
                            chordSymbol = symbol,
                            frets = voicing.frets,
                        )
                    },
                    useFlats = appSettings.display.useFlats,
                    leftHanded = appSettings.fretboard.leftHanded,
                )
                TAB_PATTERNS -> StrumPatternsTab()
                TAB_FAVORITES -> FavoritesTab(
                    viewModel = favoritesViewModel,
                    onVoicingSelected = { voicing ->
                        fretboardViewModel.applyVoicing(voicing)
                        selectedTab = TAB_EXPLORER
                    },
                    useFlats = appSettings.display.useFlats,
                    leftHanded = appSettings.fretboard.leftHanded,
                )
                TAB_SONGBOOK -> SongbookTab(
                    viewModel = songbookViewModel,
                    onChordTapped = { chordName ->
                        navigateToChord(chordName, libraryViewModel) { selectedTab = TAB_LIBRARY }
                    },
                )
                TAB_PROGRESSIONS -> ProgressionsTab(
                    useFlats = appSettings.display.useFlats,
                    onChordTapped = { rootPitchClass, quality ->
                        // Find the matching formula and load it in the library
                        libraryViewModel.selectRoot(rootPitchClass)
                        val formula = com.baijum.ukufretboard.data.ChordFormulas.ALL
                            .firstOrNull { it.symbol == quality }
                        if (formula != null) {
                            libraryViewModel.selectCategory(formula.category)
                            libraryViewModel.selectFormula(formula)
                        }
                        selectedTab = TAB_LIBRARY
                    },
                )
            }
        }
    }

    // Settings bottom sheet
    if (showSettings) {
        SettingsSheet(
            soundSettings = appSettings.sound,
            onSoundSettingsChange = { newSound ->
                settingsViewModel.updateSound { newSound }
            },
            displaySettings = appSettings.display,
            onDisplaySettingsChange = { newDisplay ->
                settingsViewModel.updateDisplay { newDisplay }
            },
            tuningSettings = appSettings.tuning,
            onTuningSettingsChange = { newTuning ->
                settingsViewModel.updateTuning { newTuning }
            },
            fretboardSettings = appSettings.fretboard,
            onFretboardSettingsChange = { newFretboard ->
                settingsViewModel.updateFretboard { newFretboard }
            },
            onDismiss = { showSettings = false },
        )
    }
}

/**
 * Navigates to the Chord Library tab with the given chord name parsed into root + quality.
 */
private fun navigateToChord(
    chordName: String,
    libraryViewModel: ChordLibraryViewModel,
    switchTab: () -> Unit,
) {
    // Parse root note: first char (A-G), optionally followed by # or b
    val rootMatch = Regex("^([A-G][#b]?)").find(chordName) ?: return
    val rootStr = rootMatch.groupValues[1]
    val quality = chordName.removePrefix(rootStr)

    // Find the pitch class for the root
    val noteNames = com.baijum.ukufretboard.data.Notes.NOTE_NAMES_SHARP
    val noteNamesFlat = com.baijum.ukufretboard.data.Notes.NOTE_NAMES_FLAT
    val rootPitchClass = noteNames.indexOf(rootStr).takeIf { it >= 0 }
        ?: noteNamesFlat.indexOf(rootStr).takeIf { it >= 0 }
        ?: return

    libraryViewModel.selectRoot(rootPitchClass)
    val formula = com.baijum.ukufretboard.data.ChordFormulas.ALL
        .firstOrNull { it.symbol == quality }
    if (formula != null) {
        libraryViewModel.selectCategory(formula.category)
        libraryViewModel.selectFormula(formula)
    }
    switchTab()
}

/**
 * Content for the Explorer tab — the original interactive fretboard UI.
 *
 * @param viewModel The [FretboardViewModel] managing fretboard state.
 * @param soundEnabled Whether sound playback is enabled (controls play button visibility).
 */
@Composable
private fun ExplorerTabContent(
    viewModel: FretboardViewModel,
    soundEnabled: Boolean,
    leftHanded: Boolean = false,
    useFlats: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Scale selector (collapsible)
        ScaleSelector(
            state = uiState.scaleOverlay,
            useFlats = useFlats,
            onRootChanged = viewModel::setScaleRoot,
            onScaleChanged = viewModel::setScale,
            onToggle = viewModel::toggleScaleOverlay,
        )

        // Interactive fretboard
        FretboardView(
            tuning = viewModel.tuning,
            selections = uiState.selections,
            showNoteNames = uiState.showNoteNames,
            onFretTap = viewModel::toggleFret,
            getNoteAt = viewModel::getNoteAt,
            leftHanded = leftHanded,
            scaleNotes = if (uiState.scaleOverlay.enabled) uiState.scaleOverlay.scaleNotes else emptySet(),
            scaleRoot = if (uiState.scaleOverlay.enabled) uiState.scaleOverlay.root else null,
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
            OutlinedButton(onClick = viewModel::toggleScaleOverlay) {
                Text(if (uiState.scaleOverlay.enabled) "Hide Scale" else "Scales")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chord detection result with play button
        ChordResultView(
            detectionResult = uiState.detectionResult,
            fingerPositions = uiState.fingerPositions,
            onPlayChord = viewModel::playChord,
            soundEnabled = soundEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
