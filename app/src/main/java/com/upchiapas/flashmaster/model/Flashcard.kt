package com.upchiapas.flashmaster.model

// enum con los niveles de dominio de la tarjeta
enum class MasteryLevel {
    POR_REPASAR,
    EN_PROCESO,
    DOMINADA
}

// modelo de la tarjeta de estudio individual
data class Flashcard(
    val id: String,
    val question: String,
    val answer: String,
    val category: String,
    val mastery: MasteryLevel = MasteryLevel.POR_REPASAR,
    val isCustom: Boolean = false
)
