package com.upchiapas.flashmaster

import android.app.Application
import com.upchiapas.flashmaster.di.FlashcardModule

// clase application que inicializa el modulo con el contexto de la app
class FlashMasterApplication : Application() {
    val flashcardModule by lazy {
        FlashcardModule(this)
    }
}
