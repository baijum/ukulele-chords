package com.baijum.ukufretboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.baijum.ukufretboard.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel managing progress tracking (learned chords, streak).
 */
class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProgressRepository(application)

    private val _learnedChords = MutableStateFlow(repository.getLearnedChords())
    private val _dailyStreak = MutableStateFlow(repository.getDailyStreak())

    /** Set of learned chord keys (e.g., "0|Major"). */
    val learnedChords: StateFlow<Set<String>> = _learnedChords.asStateFlow()

    /** Current daily practice streak. */
    val dailyStreak: StateFlow<Int> = _dailyStreak.asStateFlow()

    /** Number of chords learned. */
    val learnedCount: Int get() = _learnedChords.value.size

    fun toggleLearned(rootPitchClass: Int, quality: String) {
        if (repository.isLearned(rootPitchClass, quality)) {
            repository.unmarkLearned(rootPitchClass, quality)
        } else {
            repository.markLearned(rootPitchClass, quality)
        }
        refresh()
    }

    fun isLearned(rootPitchClass: Int, quality: String): Boolean =
        repository.isLearned(rootPitchClass, quality)

    private fun refresh() {
        _learnedChords.value = repository.getLearnedChords()
        _dailyStreak.value = repository.getDailyStreak()
    }
}
