package com.upchiapas.flashmaster.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.MasteryLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// delegado de datastore preferences a nivel de contexto
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flashmaster_prefs")

// fuente de persistencia local mediante jetpack datastore
class FlashcardPreferencesDataSource(
    private val context: Context
) {
    companion object {
        // claves de preferencias
        private val KEY_CUSTOM_CARDS = stringPreferencesKey("custom_cards_data")
        private val KEY_MASTERY_MAP = stringPreferencesKey("mastery_ratings_data")
        private const val DELIMITER_FIELD = ":::FM:::"
        private const val DELIMITER_RECORD = "###REC###"
    }

    // recupera las tarjetas creadas por el usuario guardadas en datastore
    suspend fun getCustomCards(): List<Flashcard> {
        val raw = context.dataStore.data.map { it[KEY_CUSTOM_CARDS] ?: "" }.first()
        if (raw.isBlank()) return emptyList()

        return raw.split(DELIMITER_RECORD).filter { it.isNotBlank() }.mapNotNull { record ->
            val fields = record.split(DELIMITER_FIELD)
            if (fields.size >= 5) {
                val id = fields[0]
                val question = fields[1]
                val answer = fields[2]
                val category = fields[3]
                val mastery = try {
                    MasteryLevel.valueOf(fields[4])
                } catch (e: Exception) {
                    MasteryLevel.POR_REPASAR
                }
                Flashcard(id, question, answer, category, mastery, isCustom = true)
            } else {
                null
            }
        }
    }

    // guarda una nueva tarjeta en datastore
    suspend fun saveCustomCard(card: Flashcard) {
        val existing = getCustomCards()
        val updated = existing + card
        val serialized = updated.joinToString(DELIMITER_RECORD) { c ->
            listOf(c.id, c.question, c.answer, c.category, c.mastery.name).joinToString(DELIMITER_FIELD)
        }

        context.dataStore.edit { preferences ->
            preferences[KEY_CUSTOM_CARDS] = serialized
        }
    }

    // carga el mapa de calificaciones de dominio guardadas
    suspend fun getMasteryRatings(): Map<String, MasteryLevel> {
        val raw = context.dataStore.data.map { it[KEY_MASTERY_MAP] ?: "" }.first()
        if (raw.isBlank()) return emptyMap()

        val map = mutableMapOf<String, MasteryLevel>()
        raw.split(",").forEach { pair ->
            val parts = pair.split("=")
            if (parts.size == 2) {
                try {
                    map[parts[0]] = MasteryLevel.valueOf(parts[1])
                } catch (_: Exception) {
                }
            }
        }
        return map
    }

    // actualiza el nivel de dominio de una tarjeta en datastore
    suspend fun saveMasteryRating(cardId: String, level: MasteryLevel) {
        val map = getMasteryRatings().toMutableMap()
        map[cardId] = level
        val serialized = map.entries.joinToString(",") { "${it.key}=${it.value.name}" }

        context.dataStore.edit { preferences ->
            preferences[KEY_MASTERY_MAP] = serialized
        }
    }

    // limpia las calificaciones guardadas para reiniciar el progreso
    suspend fun clearMasteryRatings() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_MASTERY_MAP)
        }
    }
}
