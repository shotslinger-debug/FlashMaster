package com.upchiapas.flashmaster.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.flashmaster.model.Flashcard
import com.upchiapas.flashmaster.model.MasteryLevel
import com.upchiapas.flashmaster.view.components.CategoryChipsRow
import com.upchiapas.flashmaster.view.components.FlashcardDisplayCard
import com.upchiapas.flashmaster.view.components.ProgressOverviewCard

// maquetado inicial de la pantalla con puro disenio estatico
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardPage() {
    // datos de ejemplo para visualizar el disenio y maquetado de componentes
    val sampleCategories = listOf("Todas", "Kotlin", "Compose", "Arquitectura")
    val sampleCard = Flashcard(
        id = "1",
        question = "¿Que es una Data Class en Kotlin y que metodos genera sola?",
        answer = "Es una clase para contener datos. Genera toString, copy, equals, hashCode y componentN automaticamente.",
        category = "Kotlin",
        mastery = MasteryLevel.EN_PROCESO
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FlashMaster",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Simulador de Estudio & Examen",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "añadir tarjeta"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // selector visual de temas
                CategoryChipsRow(
                    categories = sampleCategories,
                    selectedCategory = "Todas",
                    onCategorySelected = {}
                )
            }

            item {
                // resumen visual del progreso con barrita
                ProgressOverviewCard(
                    masteryPercentage = 0.42f,
                    countMastered = 5,
                    countInProcess = 4,
                    countToReview = 3,
                    onResetClick = {}
                )
            }

            item {
                // tarjeta interactiva central
                FlashcardDisplayCard(
                    card = sampleCard,
                    isAnswerRevealed = true,
                    currentIndex = 2,
                    totalCards = 12,
                    onToggleReveal = {},
                    onMasterySelected = {},
                    onPreviousClick = {},
                    onNextClick = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
