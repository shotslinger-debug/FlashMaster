package com.upchiapas.flashmaster.data.repository

import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.MasteryLevel

// interfaz que define el contrato del repositorio
interface FlashcardRepository {
    fun getFlashcards(): List<Flashcard>
    fun addFlashcard(card: Flashcard)
    fun updateMastery(id: String, mastery: MasteryLevel)
    fun resetProgress()
}
