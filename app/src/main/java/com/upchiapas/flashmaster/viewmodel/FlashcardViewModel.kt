package com.upchiapas.flashmaster.viewmodel

import androidx.lifecycle.ViewModel
import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.FlashcardUiState
import com.upchiapas.flashmaster.model.MasteryLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// viewmodel principal que controla la logica y datos en memoria
class FlashcardViewModel : ViewModel() {

    // backing property privada para no romper la arquitecura
    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        // aqui cargo las preguntas base directo en memoria
        cargarPreguntasIniciales()
    }

    // funcion para meter las tarjetas precargadas de la materia
    private fun cargarPreguntasIniciales() {
        val tarjetasSemilla = listOf(
            // preguntas de kotlin
            Flashcard("k1", "¿Que palabra clave se usa en Kotlin para declarar una variable inmutable?", "val", "Kotlin"),
            Flashcard("k2", "¿Cual es la diferencia principal entre val y var?", "val no permite reasignacion despues de su inicializacion y var si", "Kotlin"),
            Flashcard("k3", "¿Que es una data class y que metodos genera sola?", "sirve para guardar datos y genera toString copy equals hashcode automaticamente", "Kotlin"),
            Flashcard("k4", "¿Como se maneja la seguridad contra nulos en Kotlin?", "usando el signo de interrogacion (?) en el tipo de dato y el operador elvis (?:)", "Kotlin"),

            // preguntas de compose
            Flashcard("c1", "¿Que es la recomposicion en Jetpack Compose?", "es el proceso donde compose vuelve a ejecutar las funciones composables que dependen de un estado que cambio", "Compose"),
            Flashcard("c2", "¿Que significa que un composable sea Stateless?", "que no guarda estado interno y recibe sus valores por parametros y notifica con eventos", "Compose"),
            Flashcard("c3", "¿Para que se usa la funcion remember?", "para preservar un valor o estado en memoria a traves de las recomposiciones", "Compose"),
            Flashcard("c4", "¿Cual es la ventaja de LazyColumn sobre Column normal?", "lazycolumn solo renderiza los elementos visibles en la pantalla para ahorrar memoria", "Compose"),

            // preguntas de arquitectura
            Flashcard("a1", "¿Cual es el rol principal del ViewModel en MVVM?", "separar la logica de la vista y retener el estado ante giros de pantalla", "Arquitectura"),
            Flashcard("a2", "¿Que es el patron UDF (Unidirectional Data Flow)?", "el estado baja del viewmodel a la ui y los eventos suben de la ui al viewmodel", "Arquitectura"),
            Flashcard("a3", "¿Que ventaja ofrece StateFlow sobre LiveData en Compose?", "stateflow es nativo de kotlin coroutines y siempre tiene un valor inicial garantizado", "Arquitectura"),
            Flashcard("a4", "¿Para que sirve la Backing Property (_uiState y uiState)?", "para que la ui solo pueda leer el estado y no modificarlo directamente", "Arquitectura")
        )

        _uiState.update { estadoActual ->
            val listaFiltrada = tarjetasSemilla
            estadoActual.copy(
                cards = tarjetasSemilla,
                filteredCards = listaFiltrada
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

        // actualizo el nivel en la lista general
        val tarjetasActualizadas = estadoActual.cards.map { tarjeta ->
            if (tarjeta.id == tarjetaActual.id) {
                tarjeta.copy(mastery = level)
            } else {
                tarjeta
            }
        }

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
        val todas = _uiState.value.cards
        val filtradas = if (category == "Todas") {
            todas
        } else {
            todas.filter { it.category == category }
        }

        _uiState.update {
            it.copy(
                selectedCategory = category,
                filteredCards = filtradas,
                currentCardIndex = 0,
                isAnswerRevealed = false
            )
        }
        recalcularMetricas()
    }

    // reinicia el progreso de las tarjetas a cero para volver a practicar
    fun resetProgress() {
        val reiniciadas = _uiState.value.cards.map {
            it.copy(mastery = MasteryLevel.POR_REPASAR)
        }
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

    // valida y agrega la tarjeta inventada por el usuario en memoria
    fun saveNewCard() {
        val estado = _uiState.value
        if (estado.newQuestionInput.trim().isEmpty() || estado.newAnswerInput.trim().isEmpty()) {
            _uiState.update { it.copy(inputErrorMessage = "debes llenar la pregunta y respuesta") }
            return
        }

        val nueva = Flashcard(
            id = UUID.randomUUID().toString(),
            question = estado.newQuestionInput.trim(),
            answer = estado.newAnswerInput.trim(),
            category = estado.newCategoryInput,
            mastery = MasteryLevel.POR_REPASAR,
            isCustom = true
        )

        val nuevasTarjetas = estado.cards + nueva
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
