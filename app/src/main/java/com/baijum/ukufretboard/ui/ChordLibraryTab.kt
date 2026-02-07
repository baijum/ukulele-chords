package com.baijum.ukufretboard.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.data.ChordCategory
import com.baijum.ukufretboard.data.ChordFormula
import com.baijum.ukufretboard.data.ChordFormulas
import com.baijum.ukufretboard.data.Notes
import com.baijum.ukufretboard.domain.ChordVoicing
import com.baijum.ukufretboard.viewmodel.ChordLibraryViewModel

/**
 * The chord library tab content.
 *
 * Provides a three-level selection flow:
 * 1. Root note (12 chromatic notes)
 * 2. Category (Triad, Seventh, Suspended, Extended)
 * 3. Chord type (formulas within the selected category)
 *
 * Below the selectors, a grid of mini chord diagrams shows all playable
 * voicings. Tapping a diagram calls [onVoicingSelected] to apply it
 * to the fretboard.
 *
 * @param viewModel The [ChordLibraryViewModel] managing selection state.
 * @param onVoicingSelected Callback invoked when the user taps a voicing diagram.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
fun ChordLibraryTab(
    viewModel: ChordLibraryViewModel,
    onVoicingSelected: (ChordVoicing) -> Unit,
    onVoicingLongPressed: ((ChordVoicing) -> Unit)? = null,
    useFlats: Boolean = false,
    leftHanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        // Section: Root note selector
        SectionLabel("Root Note")
        RootNoteSelector(
            selectedRoot = uiState.selectedRoot,
            onRootSelected = viewModel::selectRoot,
            useFlats = useFlats,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section: Category selector
        SectionLabel("Type")
        CategorySelector(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = viewModel::selectCategory,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Section: Chord formula selector
        FormulaSelector(
            category = uiState.selectedCategory,
            selectedFormula = uiState.selectedFormula,
            selectedRoot = uiState.selectedRoot,
            onFormulaSelected = viewModel::selectFormula,
            useFlats = useFlats,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Section: Transpose controls
        if (uiState.selectedFormula != null) {
            TransposeControls(
                rootPitchClass = uiState.selectedRoot,
                chordSymbol = uiState.selectedFormula?.symbol ?: "",
                semitoneOffset = 0, // Stateless — offset is reflected in root selection
                originalRoot = uiState.selectedRoot,
                useFlats = useFlats,
                onTranspose = { viewModel.transpose(it) },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section: Voicing results
        if (uiState.voicings.isNotEmpty()) {
            val rootName = Notes.pitchClassToName(uiState.selectedRoot, useFlats)
            val symbol = uiState.selectedFormula?.symbol ?: ""
            SectionLabel("$rootName$symbol — ${uiState.voicings.size} voicings")
        }

        VoicingGrid(
            voicings = uiState.voicings,
            onVoicingSelected = onVoicingSelected,
            onVoicingLongPressed = onVoicingLongPressed,
            leftHanded = leftHanded,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * A small bold section label.
 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
    )
}

/**
 * Horizontal scrollable row of 12 note chips (C, C#, D, ... B).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootNoteSelector(
    selectedRoot: Int,
    onRootSelected: (Int) -> Unit,
    useFlats: Boolean = false,
) {
    val noteNames = if (useFlats) Notes.NOTE_NAMES_FLAT else Notes.NOTE_NAMES_SHARP
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        noteNames.forEachIndexed { index, name ->
            FilterChip(
                selected = index == selectedRoot,
                onClick = { onRootSelected(index) },
                label = {
                    Text(
                        text = name,
                        fontWeight = if (index == selectedRoot) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

/**
 * Row of 4 category filter chips (Triad, Seventh, Suspended, Extended).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(
    selectedCategory: ChordCategory,
    onCategorySelected: (ChordCategory) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ChordCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.label,
                        fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        }
    }
}

/**
 * Horizontal row of formula chips for the selected category.
 * Shows the chord name (root + symbol) for each formula.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormulaSelector(
    category: ChordCategory,
    selectedFormula: ChordFormula?,
    selectedRoot: Int,
    onFormulaSelected: (ChordFormula) -> Unit,
    useFlats: Boolean = false,
) {
    val formulas = ChordFormulas.BY_CATEGORY[category] ?: emptyList()
    val rootName = Notes.pitchClassToName(selectedRoot, useFlats)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        formulas.forEach { formula ->
            val isSelected = formula == selectedFormula
            FilterChip(
                selected = isSelected,
                onClick = { onFormulaSelected(formula) },
                label = {
                    Text(
                        text = "$rootName${formula.symbol}",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

/**
 * Grid of mini chord diagram cards, 2 columns wide.
 */
@Composable
private fun VoicingGrid(
    voicings: List<ChordVoicing>,
    onVoicingSelected: (ChordVoicing) -> Unit,
    onVoicingLongPressed: ((ChordVoicing) -> Unit)? = null,
    leftHanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (voicings.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No voicings found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(voicings) { voicing ->
                ChordDiagramPreview(
                    voicing = voicing,
                    onClick = { onVoicingSelected(voicing) },
                    onLongClick = onVoicingLongPressed?.let { callback -> { callback(voicing) } },
                    leftHanded = leftHanded,
                )
            }
        }
    }
}
