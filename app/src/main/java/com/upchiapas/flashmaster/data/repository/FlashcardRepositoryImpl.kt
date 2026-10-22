package com.upchiapas.flashmaster.data.repository

import com.upchiapas.flashmaster.data.datasource.FlashcardLocalDataSource
import com.upchiapas.flashmaster.data.datasource.FlashcardPreferencesDataSource
import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.MasteryLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// implementacion del repositorio que combina memoria con persistencia en datastore
class FlashcardRepositoryImpl(
    private val localDataSource: FlashcardLocalDataSource,
    private val preferencesDataSource: FlashcardPreferencesDataSource
) : FlashcardRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // al arrancar recuperamos las tarjetas y calificaciones guardadas en datastore
        cargarDatosDeDataStore()
    }

    private fun cargarDatosDeDataStore() {
        try {
            runBlocking(Dispatchers.IO) {
                // cargamos tarjetas personalizadas del usuario
                val guardadas = preferencesDataSource.getCustomCards()
                guardadas.forEach { localDataSource.addCard(it) }

                // cargamos las calificaciones de dominio guardadas
                val ratings = preferencesDataSource.getMasteryRatings()
                ratings.forEach { (id, level) ->
                    localDataSource.updateCardMastery(id, level)
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun getFlashcards(): List<Flashcard> {
        return localDataSource.getCards()
    }

    override fun addFlashcard(card: Flashcard) {
        localDataSource.addCard(card)
        // persistimos la tarjeta en datastore en segundo plano
        scope.launch {
            preferencesDataSource.saveCustomCard(card)
        }
    }

    override fun updateMastery(id: String, mastery: MasteryLevel) {
        localDataSource.updateCardMastery(id, mastery)
        // persistimos la calificacion en datastore en segundo plano
        scope.launch {
            preferencesDataSource.saveMasteryRating(id, mastery)
        }
    }

    override fun resetProgress() {
        localDataSource.resetAllMastery()
        // limpiamos las calificaciones de datastore
        scope.launch {
            preferencesDataSource.clearMasteryRatings()
        }
    }
}
