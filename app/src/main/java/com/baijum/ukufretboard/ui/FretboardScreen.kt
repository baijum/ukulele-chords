package com.baijum.ukufretboard.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.baijum.ukufretboard.audio.ToneGenerator
import com.baijum.ukufretboard.viewmodel.ChordLibraryViewModel
import com.baijum.ukufretboard.viewmodel.FavoritesViewModel
import com.baijum.ukufretboard.viewmodel.FretboardViewModel
import com.baijum.ukufretboard.viewmodel.SettingsViewModel
import com.baijum.ukufretboard.viewmodel.SongbookViewModel
import com.baijum.ukufretboard.viewmodel.SyncViewModel

/** Navigation section indices. */
private const val NAV_EXPLORER = 0
private const val NAV_LIBRARY = 1
private const val NAV_PATTERNS = 2
private const val NAV_PROGRESSIONS = 3
private const val NAV_FAVORITES = 4
private const val NAV_SONGBOOK = 5

/**
 * Drawer navigation item metadata.
 */
private data class DrawerItem(
    val index: Int,
    val label: String,
    val icon: ImageVector,
)

/** Items displayed in the navigation drawer. Built lazily inside the composable. */
private fun drawerItems(): List<DrawerItem> = listOf(
    DrawerItem(NAV_EXPLORER, "Explorer", Icons.Filled.Home),
    DrawerItem(NAV_LIBRARY, "Chords", Icons.Filled.Search),
    DrawerItem(NAV_PATTERNS, "Patterns", Icons.AutoMirrored.Filled.List),
    DrawerItem(NAV_PROGRESSIONS, "Progressions", Icons.Filled.PlayArrow),
    DrawerItem(NAV_FAVORITES, "Favorites", Icons.Filled.Favorite),
    DrawerItem(NAV_SONGBOOK, "Songs", Icons.Filled.Create),
)

/**
 * Top-level screen composable for the Ukulele Chord Explorer.
 *
 * Uses a [ModalNavigationDrawer] to navigate between sections:
 * Explorer, Chords, Patterns, Progressions, Favorites, and Songs.
 *
 * A hamburger menu icon in the top app bar opens the drawer.
 * Both Explorer and Chord Library share the same [FretboardViewModel]
 * so that selecting a voicing in the library can load it onto the
 * explorer's fretboard.
 *
 * @param fretboardViewModel The shared [FretboardViewModel] instance.
 * @param libraryViewModel The [ChordLibraryViewModel] for the library section.
 * @param settingsViewModel The shared [SettingsViewModel] for app-wide settings.
 * @param favoritesViewModel The [FavoritesViewModel] for managing favorites.
 * @param songbookViewModel The [SongbookViewModel] for managing chord sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FretboardScreen(
    fretboardViewModel: FretboardViewModel = viewModel(),
    libraryViewModel: ChordLibraryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel(),
    songbookViewModel: SongbookViewModel = viewModel(),
    syncViewModel: SyncViewModel = viewModel(),
) {
    var selectedSection by remember { mutableIntStateOf(NAV_EXPLORER) }
    var showSettings by remember { mutableStateOf(false) }

    // Initialize SyncViewModel with SettingsViewModel reference
    LaunchedEffect(Unit) {
        syncViewModel.init(settingsViewModel)
    }

    // Initialize sampled audio engine (loads OGG samples into SoundPool)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ToneGenerator.init(context)
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = remember { drawerItems() }

    val appSettings by settingsViewModel.settings.collectAsState()

    // Collect favorites so that isFavorite checks trigger recomposition
    val currentFavorites by favoritesViewModel.favorites.collectAsState()

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Ukulele Chord Explorer",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                )
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedSection == item.index,
                        onClick = {
                            selectedSection = item.index
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = items.firstOrNull { it.index == selectedSection }?.label
                                ?: "Explorer",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open navigation menu",
                            )
                        }
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
                // Section content
                when (selectedSection) {
                    NAV_EXPLORER -> ExplorerTabContent(
                        viewModel = fretboardViewModel,
                        soundEnabled = appSettings.sound.enabled,
                        leftHanded = appSettings.fretboard.leftHanded,
                        useFlats = appSettings.display.useFlats,
                    )
                    NAV_LIBRARY -> ChordLibraryTab(
                        viewModel = libraryViewModel,
                        onVoicingSelected = { voicing ->
                            fretboardViewModel.applyVoicing(voicing)
                            selectedSection = NAV_EXPLORER
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
                    isFavorite = { voicing ->
                        // Reference currentFavorites to trigger recomposition on change
                        val state = libraryViewModel.uiState.value
                        val symbol = state.selectedFormula?.symbol ?: ""
                        val key = "${state.selectedRoot}|$symbol|${voicing.frets.joinToString(",")}"
                        currentFavorites.any { it.key == key }
                    },
                        onToggleFavorite = { voicing ->
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
                    NAV_PATTERNS -> StrumPatternsTab()
                    NAV_PROGRESSIONS -> ProgressionsTab(
                        useFlats = appSettings.display.useFlats,
                        onChordTapped = { rootPitchClass, quality ->
                            libraryViewModel.selectRoot(rootPitchClass)
                            val formula = com.baijum.ukufretboard.data.ChordFormulas.ALL
                                .firstOrNull { it.symbol == quality }
                            if (formula != null) {
                                libraryViewModel.selectCategory(formula.category)
                                libraryViewModel.selectFormula(formula)
                            }
                            selectedSection = NAV_LIBRARY
                        },
                    )
                    NAV_FAVORITES -> FavoritesTab(
                        viewModel = favoritesViewModel,
                        onVoicingSelected = { voicing ->
                            fretboardViewModel.applyVoicing(voicing)
                            selectedSection = NAV_EXPLORER
                        },
                        useFlats = appSettings.display.useFlats,
                        leftHanded = appSettings.fretboard.leftHanded,
                    )
                    NAV_SONGBOOK -> SongbookTab(
                        viewModel = songbookViewModel,
                        onChordTapped = { chordName ->
                            navigateToChord(chordName, libraryViewModel) { selectedSection = NAV_LIBRARY }
                        },
                    )
                }
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
            syncViewModel = syncViewModel,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
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

        // Chord detection result with play button and detailed info
        val fretsList = uiState.selections.entries
            .sortedBy { it.key }
            .map { it.value ?: 0 }

        ChordResultView(
            detectionResult = uiState.detectionResult,
            fingerPositions = uiState.fingerPositions,
            onPlayChord = viewModel::playChord,
            soundEnabled = soundEnabled,
            frets = fretsList,
            useFlats = useFlats,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
