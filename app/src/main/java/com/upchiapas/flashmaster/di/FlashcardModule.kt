package com.upchiapas.flashmaster.di

import com.upchiapas.flashmaster.data.datasource.FlashcardLocalDataSource
import com.upchiapas.flashmaster.data.repository.FlashcardRepository
import com.upchiapas.flashmaster.data.repository.FlashcardRepositoryImpl
import com.upchiapas.flashmaster.viewmodel.FlashcardViewModelFactory

// contenedor de inyeccion de dependencias manual
class FlashcardModule {

    private val localDataSource: FlashcardLocalDataSource by lazy {
        FlashcardLocalDataSource()
    }

    val repository: FlashcardRepository by lazy {
        FlashcardRepositoryImpl(localDataSource)
    }

    val factoryFlashcard: FlashcardViewModelFactory by lazy {
        FlashcardViewModelFactory(repository)
    }
}
