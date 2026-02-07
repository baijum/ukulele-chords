package com.baijum.ukufretboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.baijum.ukufretboard.data.ChordSheet
import com.baijum.ukufretboard.data.ChordSheetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing the songbook (list of chord sheets).
 */
class SongbookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChordSheetRepository(application)

    private val _sheets = MutableStateFlow<List<ChordSheet>>(emptyList())

    /** Observable list of all chord sheets. */
    val sheets: StateFlow<List<ChordSheet>> = _sheets.asStateFlow()

    /** The currently open sheet (for viewing/editing). */
    private val _currentSheet = MutableStateFlow<ChordSheet?>(null)
    val currentSheet: StateFlow<ChordSheet?> = _currentSheet.asStateFlow()

    /** Whether the editor is open. */
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _sheets.value = repository.getAll()
    }

    fun openSheet(sheet: ChordSheet) {
        _currentSheet.value = sheet
        _isEditing.value = false
    }

    fun closeSheet() {
        _currentSheet.value = null
        _isEditing.value = false
    }

    fun startEditing(sheet: ChordSheet? = null) {
        _currentSheet.value = sheet ?: ChordSheet(title = "", content = "")
        _isEditing.value = true
    }

    fun saveSheet(title: String, artist: String, content: String) {
        val existing = _currentSheet.value
        val sheet = if (existing != null && existing.title.isNotEmpty()) {
            existing.copy(
                title = title,
                artist = artist,
                content = content,
                updatedAt = System.currentTimeMillis(),
            )
        } else {
            ChordSheet(
                title = title,
                artist = artist,
                content = content,
            )
        }
        repository.save(sheet)
        _currentSheet.value = sheet
        _isEditing.value = false
        refresh()
    }

    fun deleteSheet(id: String) {
        repository.delete(id)
        if (_currentSheet.value?.id == id) {
            _currentSheet.value = null
        }
        refresh()
    }
}
