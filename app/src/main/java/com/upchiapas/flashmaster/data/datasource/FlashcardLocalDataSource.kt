package com.upchiapas.flashmaster.data.datasource

import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.MasteryLevel

// fuente de datos local que maneja las preguntas y tarjetas en memoria
class FlashcardLocalDataSource {

    private val cards = mutableListOf(
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

    fun getCards(): List<Flashcard> = cards.toList()

    fun addCard(card: Flashcard) {
        cards.add(card)
    }

    fun updateCardMastery(id: String, mastery: MasteryLevel) {
        val index = cards.indexOfFirst { it.id == id }
        if (index != -1) {
            cards[index] = cards[index].copy(mastery = mastery)
        }
    }

    fun resetAllMastery() {
        for (i in cards.indices) {
            cards[i] = cards[i].copy(mastery = MasteryLevel.POR_REPASAR)
        }
    }
}
