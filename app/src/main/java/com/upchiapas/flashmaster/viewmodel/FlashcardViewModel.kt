package com.upchiapas.flashmaster.viewmodel

import androidx.lifecycle.ViewModel
import com.upchiapas.flashmaster.data.repository.FlashcardRepository
import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.FlashcardUiState
import com.upchiapas.flashmaster.model.MasteryLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// viewmodel principal que controla la logica y se comunica con el repository
class FlashcardViewModel(
    private val repository: FlashcardRepository
) : ViewModel() {

    // backing property privada para el flujo unidireccional
    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        // cargo las preguntas desde el repositorio
        cargarTarjetas()
    }

    private fun cargarTarjetas() {
        val tarjetas = repository.getFlashcards()
        _uiState.update { estado ->
            estado.copy(
                cards = tarjetas,
                filteredCards = tarjetas
            )
        }
        recalcularMetricas()
    }

    // voltea o descubre la respuesta de la tarjeta actual
    fun toggleRevealAnswer() {
        _uiState.update { it.copy(isAnswerRevealed = !it.isAnswerRevealed) }
    }

    // le asigna calificacion a la tarjeta y pasa a la que sigue
    fun setMastery(level: MasteryLevel) {
        val estadoActual = _uiState.value
        val listaFiltrada = estadoActual.filteredCards
        if (listaFiltrada.isEmpty()) return

        val tarjetaActual = listaFiltrada[estadoActual.currentCardIndex]

        // actualizo el estado en el repositorio
        repository.updateMastery(tarjetaActual.id, level)

        // obtengo la lista actualizada del repositorio
        val tarjetasActualizadas = repository.getFlashcards()

        // saco la nueva lista filtrada
        val nuevaFiltrada = if (estadoActual.selectedCategory == "Todas") {
            tarjetasActualizadas
        } else {
            tarjetasActualizadas.filter { it.category == estadoActual.selectedCategory }
        }

        // calculo el siguiente indice
        val proximoIndice = if (estadoActual.currentCardIndex + 1 < nuevaFiltrada.size) {
            estadoActual.currentCardIndex + 1
        } else {
            0
        }

        _uiState.update {
            it.copy(
                cards = tarjetasActualizadas,
                filteredCards = nuevaFiltrada,
                currentCardIndex = proximoIndice,
                isAnswerRevealed = false
            )
        }
        recalcularMetricas()
    }

    // avanza a la siguiente tarjeta
    fun nextCard() {
        val total = _uiState.value.filteredCards.size
        if (total > 0) {
            val nuevo = (_uiState.value.currentCardIndex + 1) % total
            _uiState.update { it.copy(currentCardIndex = nuevo, isAnswerRevealed = false) }
        }
    }

    // retrocede a la tarjeta anterior
    fun previousCard() {
        val total = _uiState.value.filteredCards.size
        if (total > 0) {
            val nuevo = if (_uiState.value.currentCardIndex - 1 < 0) total - 1 else _uiState.value.currentCardIndex - 1
            _uiState.update { it.copy(currentCardIndex = nuevo, isAnswerRevealed = false) }
        }
    }

    // filtra las tarjetas por el chip que presiono el usuario
    fun selectCategory(category: String) {
        val todas = repository.getFlashcards()
        val filtradas = if (category == "Todas") {
            todas
        } else {
            todas.filter { it.category == category }
        }

        _uiState.update {
            it.copy(
                selectedCategory = category,
                cards = todas,
                filteredCards = filtradas,
                currentCardIndex = 0,
                isAnswerRevealed = false
            )
        }
        recalcularMetricas()
    }

    // reinicia el progreso de las tarjetas mediante el repositorio
    fun resetProgress() {
        repository.resetProgress()
        val reiniciadas = repository.getFlashcards()
        val filtradas = if (_uiState.value.selectedCategory == "Todas") {
            reiniciadas
        } else {
            reiniciadas.filter { it.category == _uiState.value.selectedCategory }
        }

        _uiState.update {
            it.copy(
                cards = reiniciadas,
                filteredCards = filtradas,
                currentCardIndex = 0,
                isAnswerRevealed = false
            )
        }
        recalcularMetricas()
    }

    // abre el modal para crear una nueva tarjeta
    fun openAddDialog() {
        _uiState.update {
            it.copy(
                isAddDialogOpen = true,
                newQuestionInput = "",
                newAnswerInput = "",
                inputErrorMessage = null
            )
        }
    }

    // cierra el modal
    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false, inputErrorMessage = null) }
    }

    fun onQuestionChange(text: String) {
        _uiState.update { it.copy(newQuestionInput = text, inputErrorMessage = null) }
    }

    fun onAnswerChange(text: String) {
        _uiState.update { it.copy(newAnswerInput = text, inputErrorMessage = null) }
    }

    fun onCategoryChange(cat: String) {
        _uiState.update { it.copy(newCategoryInput = cat) }
    }

    // valida y guarda la tarjeta en el repositorio, retorna true si fue exitoso
    fun saveNewCard(): Boolean {
        val estado = _uiState.value
        if (estado.newQuestionInput.trim().isEmpty() || estado.newAnswerInput.trim().isEmpty()) {
            _uiState.update { it.copy(inputErrorMessage = "debes llenar la pregunta y respuesta") }
            return false
        }

        val nueva = Flashcard(
            id = UUID.randomUUID().toString(),
            question = estado.newQuestionInput.trim(),
            answer = estado.newAnswerInput.trim(),
            category = estado.newCategoryInput,
            mastery = MasteryLevel.POR_REPASAR,
            isCustom = true
        )

        // guardo en el repositorio
        repository.addFlashcard(nueva)

        val nuevasTarjetas = repository.getFlashcards()
        val nuevasCategorias = if (estado.availableCategories.contains(nueva.category)) {
            estado.availableCategories
        } else {
            estado.availableCategories + nueva.category
        }

        val nuevasFiltradas = if (estado.selectedCategory == "Todas" || estado.selectedCategory == nueva.category) {
            if (estado.selectedCategory == "Todas") nuevasTarjetas else nuevasTarjetas.filter { it.category == estado.selectedCategory }
        } else {
            estado.filteredCards
        }

        _uiState.update {
            it.copy(
                cards = nuevasTarjetas,
                filteredCards = nuevasFiltradas,
                availableCategories = nuevasCategorias,
                isAddDialogOpen = false,
                newQuestionInput = "",
                newAnswerInput = "",
                inputErrorMessage = null
            )
        }
        recalcularMetricas()
        return true
    }

    // calcula el porcentaje de dominio y contadores
    private fun recalcularMetricas() {
        val tarjetas = _uiState.value.filteredCards
        val total = tarjetas.size
        if (total == 0) {
            _uiState.update {
                it.copy(
                    countMastered = 0,
                    countInProcess = 0,
                    countToReview = 0,
                    masteryPercentage = 0f
                )
            }
            return
        }

        val dominadas = tarjetas.count { it.mastery == MasteryLevel.DOMINADA }
        val enProceso = tarjetas.count { it.mastery == MasteryLevel.EN_PROCESO }
        val porRepasar = tarjetas.count { it.mastery == MasteryLevel.POR_REPASAR }
        val porcentaje = (dominadas.toFloat() / total.toFloat())

        _uiState.update {
            it.copy(
                countMastered = dominadas,
                countInProcess = enProceso,
                countToReview = porRepasar,
                masteryPercentage = porcentaje
            )
        }
    }
}
