package com.baijum.ukufretboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.baijum.ukufretboard.data.FavoriteFolder
import com.baijum.ukufretboard.data.FavoriteVoicing
import com.baijum.ukufretboard.data.FavoritesRepository
import com.baijum.ukufretboard.data.Notes
import com.baijum.ukufretboard.data.VoicingGenerator
import com.baijum.ukufretboard.domain.ChordVoicing
import com.baijum.ukufretboard.domain.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel managing the chord favorites list.
 *
 * Uses [AndroidViewModel] to access the application context for
 * SharedPreferences-based persistence.
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoritesRepository(application)

    private val _favorites = MutableStateFlow<List<FavoriteVoicing>>(emptyList())
    private val _folders = MutableStateFlow<List<FavoriteFolder>>(emptyList())

    /** Observable list of favorite voicings. */
    val favorites: StateFlow<List<FavoriteVoicing>> = _favorites.asStateFlow()

    /** Observable list of folders. */
    val folders: StateFlow<List<FavoriteFolder>> = _folders.asStateFlow()

    init {
        refresh()
    }

    /**
     * Adds a voicing to favorites.
     */
    fun addFavorite(rootPitchClass: Int, chordSymbol: String, frets: List<Int>) {
        val voicing = FavoriteVoicing(
            rootPitchClass = rootPitchClass,
            chordSymbol = chordSymbol,
            frets = frets,
        )
        repository.add(voicing)
        refresh()
    }

    /**
     * Removes a voicing from favorites.
     */
    fun removeFavorite(favorite: FavoriteVoicing) {
        repository.remove(favorite)
        refresh()
    }

    /**
     * Toggles a voicing in/out of favorites.
     */
    fun toggleFavorite(rootPitchClass: Int, chordSymbol: String, frets: List<Int>) {
        val voicing = FavoriteVoicing(rootPitchClass = rootPitchClass, chordSymbol = chordSymbol, frets = frets)
        if (repository.contains(voicing)) {
            repository.remove(voicing)
        } else {
            repository.add(voicing)
        }
        refresh()
    }

    /**
     * Checks if a voicing is in favorites.
     */
    fun isFavorite(rootPitchClass: Int, chordSymbol: String, frets: List<Int>): Boolean =
        repository.contains(rootPitchClass, chordSymbol, frets)

    /**
     * Converts a [FavoriteVoicing] to a [ChordVoicing] for display and application.
     */
    fun toChordVoicing(favorite: FavoriteVoicing, useFlats: Boolean = false): ChordVoicing {
        val tuning = FretboardViewModel.STANDARD_TUNING
        val notes = favorite.frets.mapIndexed { i, fret ->
            val pc = (tuning[i].openPitchClass + fret) % Notes.PITCH_CLASS_COUNT
            Note(pitchClass = pc, name = Notes.pitchClassToName(pc, useFlats))
        }
        val frettedPositions = favorite.frets.filter { it > 0 }
        return ChordVoicing(
            frets = favorite.frets,
            notes = notes,
            minFret = frettedPositions.minOrNull() ?: 0,
            maxFret = frettedPositions.maxOrNull() ?: 0,
        )
    }

    // ── Folder management ────────────────────────────────────────

    fun createFolder(name: String) {
        repository.saveFolder(FavoriteFolder(name = name))
        refresh()
    }

    fun deleteFolder(folderId: String) {
        repository.deleteFolder(folderId)
        refresh()
    }

    fun moveToFolder(favorite: FavoriteVoicing, folderId: String?) {
        repository.setFolder(favorite, folderId)
        refresh()
    }

    private fun refresh() {
        _favorites.value = repository.getAll()
        _folders.value = repository.getAllFolders()
    }
}
