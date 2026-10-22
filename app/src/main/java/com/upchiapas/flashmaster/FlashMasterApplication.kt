package com.upchiapas.flashmaster

import android.app.Application
import com.upchiapas.flashmaster.di.FlashcardModule

// clase application para mantener vivo el contenedor de dependencias
class FlashMasterApplication : Application() {
    val flashcardModule by lazy {
        FlashcardModule()
    }
}
