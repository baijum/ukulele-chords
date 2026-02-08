package com.baijum.ukufretboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.baijum.ukufretboard.data.ChordDegree
import com.baijum.ukufretboard.data.CustomProgression
import com.baijum.ukufretboard.data.CustomProgressionRepository
import com.baijum.ukufretboard.data.Progression
import com.baijum.ukufretboard.data.ScaleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel managing user-created chord progressions.
 *
 * Uses [AndroidViewModel] to access the application context for
 * SharedPreferences-based persistence via [CustomProgressionRepository].
 */
class CustomProgressionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CustomProgressionRepository(application)

    private val _progressions = MutableStateFlow<List<CustomProgression>>(emptyList())

    /** Observable list of custom progressions. */
    val progressions: StateFlow<List<CustomProgression>> = _progressions.asStateFlow()

    init {
        refresh()
    }

    /**
     * Creates and saves a new custom progression.
     *
     * @param name User-provided name for the progression.
     * @param description User-provided description for the progression.
     * @param degrees The sequence of chord degrees.
     * @param scaleType The scale type (Major/Minor).
     */
    fun create(
        name: String,
        description: String,
        degrees: List<ChordDegree>,
        scaleType: ScaleType,
    ) {
        val custom = CustomProgression(
            progression = Progression(
                name = name,
                description = description.ifBlank { "Custom progression" },
                degrees = degrees,
                scaleType = scaleType,
            ),
        )
        repository.save(custom)
        refresh()
    }

    /**
     * Updates an existing custom progression, preserving its ID and creation time.
     *
     * @param id The ID of the progression to update.
     * @param name Updated name.
     * @param description Updated description.
     * @param degrees Updated chord degree sequence.
     * @param scaleType Updated scale type (Major/Minor).
     */
    fun update(
        id: String,
        name: String,
        description: String,
        degrees: List<ChordDegree>,
        scaleType: ScaleType,
    ) {
        val existing = _progressions.value.find { it.id == id } ?: return
        val updated = existing.copy(
            progression = Progression(
                name = name,
                description = description.ifBlank { "Custom progression" },
                degrees = degrees,
                scaleType = scaleType,
            ),
        )
        repository.save(updated)
        refresh()
    }

    /**
     * Deletes a custom progression by its ID.
     */
    fun delete(id: String) {
        repository.delete(id)
        refresh()
    }

    private fun refresh() {
        _progressions.value = repository.getAll()
    }
}
