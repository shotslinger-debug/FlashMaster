package com.upchiapas.flashmaster.di

import android.content.Context
import com.upchiapas.flashmaster.data.datasource.FlashcardLocalDataSource
import com.upchiapas.flashmaster.data.datasource.FlashcardPreferencesDataSource
import com.upchiapas.flashmaster.data.repository.FlashcardRepository
import com.upchiapas.flashmaster.data.repository.FlashcardRepositoryImpl
import com.upchiapas.flashmaster.viewmodel.FlashcardViewModelFactory

// contenedor de inyeccion de dependencias manual con persistencia datastore
class FlashcardModule(
    private val context: Context
) {

    private val localDataSource: FlashcardLocalDataSource by lazy {
        FlashcardLocalDataSource()
    }

    private val preferencesDataSource: FlashcardPreferencesDataSource by lazy {
        FlashcardPreferencesDataSource(context)
    }

    val repository: FlashcardRepository by lazy {
        FlashcardRepositoryImpl(localDataSource, preferencesDataSource)
    }

    val factoryFlashcard: FlashcardViewModelFactory by lazy {
        FlashcardViewModelFactory(repository)
    }
}
