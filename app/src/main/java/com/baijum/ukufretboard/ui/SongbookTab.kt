package com.baijum.ukufretboard.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.baijum.ukufretboard.data.ChordParser
import com.baijum.ukufretboard.data.ChordSheet
import com.baijum.ukufretboard.viewmodel.SongbookViewModel

/**
 * Songbook tab showing a list of chord sheets, a viewer, and an editor.
 *
 * @param viewModel The [SongbookViewModel] managing chord sheets.
 * @param onChordTapped Callback when a chord name in a sheet is tapped.
 */
@Composable
fun SongbookTab(
    viewModel: SongbookViewModel,
    onChordTapped: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheets by viewModel.sheets.collectAsState()
    val currentSheet by viewModel.currentSheet.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    when {
        isEditing -> {
            SheetEditor(
                sheet = currentSheet,
                onSave = { title, artist, content ->
                    viewModel.saveSheet(title, artist, content)
                },
                onCancel = { viewModel.closeSheet() },
            )
        }
        currentSheet != null -> {
            SheetViewer(
                sheet = currentSheet!!,
                onBack = { viewModel.closeSheet() },
                onEdit = { viewModel.startEditing(currentSheet) },
                onDelete = {
                    viewModel.deleteSheet(currentSheet!!.id)
                    viewModel.closeSheet()
                },
                onChordTapped = onChordTapped,
            )
        }
        else -> {
            SheetList(
                sheets = sheets,
                onSheetTapped = { viewModel.openSheet(it) },
                onNewSheet = { viewModel.startEditing() },
                modifier = modifier,
            )
        }
    }
}

/**
 * List of saved chord sheets.
 */
@Composable
private fun SheetList(
    sheets: List<ChordSheet>,
    onSheetTapped: (ChordSheet) -> Unit,
    onNewSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (sheets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No songs yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to create your first chord sheet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sheets) { sheet ->
                    SheetCard(sheet = sheet, onClick = { onSheetTapped(sheet) })
                }
            }
        }

        FloatingActionButton(
            onClick = onNewSheet,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New chord sheet")
        }
    }
}

@Composable
private fun SheetCard(sheet: ChordSheet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sheet.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (sheet.artist.isNotEmpty()) {
                Text(
                    text = sheet.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Viewer for a chord sheet with tappable chord names.
 */
@Composable
private fun SheetViewer(
    sheet: ChordSheet,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChordTapped: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = sheet.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }

        if (sheet.artist.isNotEmpty()) {
            Text(
                text = sheet.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp, bottom = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content with tappable chords
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            sheet.content.lines().forEach { line ->
                val segments = ChordParser.parseLine(line)
                if (segments.isEmpty()) {
                    Text(
                        text = " ", // empty line
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                    )
                } else {
                    val chordColor = MaterialTheme.colorScheme.primary
                    val annotated = buildAnnotatedString {
                        segments.forEach { segment ->
                            when (segment) {
                                is ChordParser.TextSegment.PlainText -> {
                                    append(segment.text)
                                }
                                is ChordParser.TextSegment.Chord -> {
                                    val chordName = segment.name
                                    withLink(
                                        LinkAnnotation.Clickable(
                                            tag = chordName,
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    color = chordColor,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                            ),
                                            linkInteractionListener = {
                                                onChordTapped(chordName)
                                            },
                                        )
                                    ) {
                                        append(chordName)
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Editor for creating/editing a chord sheet.
 */
@Composable
private fun SheetEditor(
    sheet: ChordSheet?,
    onSave: (title: String, artist: String, content: String) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember(sheet?.id) { mutableStateOf(sheet?.title ?: "") }
    var artist by remember(sheet?.id) { mutableStateOf(sheet?.artist ?: "") }
    var content by remember(sheet?.id) { mutableStateOf(sheet?.content ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = artist,
            onValueChange = { artist = it },
            label = { Text("Artist (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Use [ChordName] for chords, e.g. Some[C]where over the [Em]rainbow",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Lyrics with [chords]") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { onSave(title, artist, content) },
                enabled = title.isNotBlank(),
            ) {
                Text("Save")
            }
        }
    }
}
