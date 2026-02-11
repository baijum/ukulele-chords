package com.baijum.ukufretboard.data

import android.content.Context
import android.content.SharedPreferences
import com.baijum.ukufretboard.domain.SrsScheduler
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for persisting SRS (Spaced Repetition System) cards.
 *
 * Each card represents a chord voicing that the user is learning.
 * Cards store SM-2 scheduling parameters and are due for review
 * based on their next review date.
 */
class SrsCardRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Gets all SRS cards.
     */
    fun getAll(): List<SrsCard> {
        val raw = prefs.getString(KEY_CARDS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<SrsCard>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Saves an SRS card (creates or updates).
     */
    fun save(card: SrsCard) {
        val cards = getAll().toMutableList()
        val index = cards.indexOfFirst { it.id == card.id }
        if (index >= 0) {
            cards[index] = card
        } else {
            cards.add(card)
        }
        prefs.edit().putString(KEY_CARDS, json.encodeToString(cards)).apply()
    }

    /**
     * Removes an SRS card by ID.
     */
    fun remove(id: String) {
        val cards = getAll().filter { it.id != id }
        prefs.edit().putString(KEY_CARDS, json.encodeToString(cards)).apply()
    }

    /**
     * Returns cards that are due for review (nextReviewDate <= now).
     */
    fun getDueCards(): List<SrsCard> {
        val now = System.currentTimeMillis()
        return getAll().filter { it.nextReviewDate <= now }
    }

    /**
     * Returns the count of cards due for review.
     */
    fun dueCount(): Int = getDueCards().size

    /**
     * Returns the total number of cards.
     */
    fun totalCount(): Int = getAll().size

    /**
     * Adds a chord voicing as a new SRS card.
     *
     * @param rootPitchClass The root note pitch class (0–11).
     * @param chordSymbol The chord quality symbol (e.g., "m", "7", "").
     * @param frets The fret positions for the voicing.
     * @return The created card, or null if a card for this voicing already exists.
     */
    fun addCard(rootPitchClass: Int, chordSymbol: String, frets: List<Int>): SrsCard? {
        val id = "$rootPitchClass|$chordSymbol|${frets.joinToString(",")}"
        val existing = getAll().firstOrNull { it.id == id }
        if (existing != null) return null

        val chordName = Notes.pitchClassToName(rootPitchClass) + chordSymbol
        val card = SrsCard(
            id = id,
            chordName = chordName,
            rootPitchClass = rootPitchClass,
            chordSymbol = chordSymbol,
            frets = frets,
        )
        save(card)
        return card
    }

    /**
     * Records a review of a card and updates its scheduling.
     *
     * @param cardId The card ID.
     * @param grade The user's self-assessed quality of recall.
     */
    fun recordReview(cardId: String, grade: SrsScheduler.Grade) {
        val card = getAll().firstOrNull { it.id == cardId } ?: return
        val result = SrsScheduler.schedule(
            currentInterval = card.interval,
            currentEaseFactor = card.easeFactor,
            grade = grade,
            repetitions = card.repetitions,
        )

        val now = System.currentTimeMillis()
        val nextReviewMs = if (result.interval == 0) {
            now + 10 * 60 * 1000L // Review again in 10 minutes
        } else {
            now + result.interval * 24L * 60L * 60L * 1000L // Days to ms
        }

        val updated = card.copy(
            interval = result.interval,
            easeFactor = result.easeFactor,
            repetitions = result.repetitions,
            nextReviewDate = nextReviewMs,
            lastReviewDate = now,
            totalReviews = card.totalReviews + 1,
        )
        save(updated)
    }

    /** Clears all SRS data. */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "srs_cards"
        private const val KEY_CARDS = "cards"
    }
}

/**
 * An SRS card representing a chord voicing to learn.
 *
 * @property id Unique identifier (rootPitchClass|symbol|frets).
 * @property chordName Human-readable chord name (e.g., "Am").
 * @property rootPitchClass Root note pitch class (0–11).
 * @property chordSymbol Chord quality symbol.
 * @property frets Fret positions for the voicing.
 * @property interval Current review interval in days.
 * @property easeFactor SM-2 ease factor.
 * @property repetitions Consecutive successful review count.
 * @property nextReviewDate Timestamp when next review is due.
 * @property lastReviewDate Timestamp of last review.
 * @property totalReviews Total number of reviews completed.
 */
@Serializable
data class SrsCard(
    val id: String,
    val chordName: String,
    val rootPitchClass: Int,
    val chordSymbol: String,
    val frets: List<Int>,
    val interval: Int = 0,
    val easeFactor: Float = SrsScheduler.DEFAULT_EASE_FACTOR,
    val repetitions: Int = 0,
    val nextReviewDate: Long = 0L,
    val lastReviewDate: Long = 0L,
    val totalReviews: Int = 0,
)
