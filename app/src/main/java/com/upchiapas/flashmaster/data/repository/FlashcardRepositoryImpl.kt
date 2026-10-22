package com.upchiapas.flashmaster.data.repository

import com.upchiapas.flashmaster.data.datasource.FlashcardLocalDataSource
import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.MasteryLevel

// implementacion del repositorio conectada a la fuente de datos local
class FlashcardRepositoryImpl(
    private val dataSource: FlashcardLocalDataSource
) : FlashcardRepository {

    override fun getFlashcards(): List<Flashcard> {
        return dataSource.getCards()
    }

    override fun addFlashcard(card: Flashcard) {
        dataSource.addCard(card)
    }

    override fun updateMastery(id: String, mastery: MasteryLevel) {
        dataSource.updateCardMastery(id, mastery)
    }

    override fun resetProgress() {
        dataSource.resetAllMastery()
    }
}
