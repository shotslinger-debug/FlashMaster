package com.upchiapas.flashmaster.model

// estado de la pantalla para el flujo ufd
data class FlashcardUiState(
    val cards: List<Flashcard> = emptyList(),
    val filteredCards: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val isAnswerRevealed: Boolean = false,
    val selectedCategory: String = "Todas",
    val availableCategories: List<String> = listOf("Todas", "Kotlin", "Compose", "Arquitectura"),
    val masteryPercentage: Float = 0f,
    val countMastered: Int = 0,
    val countInProcess: Int = 0,
    val countToReview: Int = 0,
    val isAddDialogOpen: Boolean = false,
    val newQuestionInput: String = "",
    val newAnswerInput: String = "",
    val newCategoryInput: String = "Kotlin",
    val inputErrorMessage: String? = null
)
