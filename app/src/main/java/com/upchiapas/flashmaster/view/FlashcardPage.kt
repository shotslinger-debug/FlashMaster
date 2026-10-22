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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.flashmaster.FlashMasterApplication
import com.upchiapas.flashmaster.view.components.AddCardDialog
import com.upchiapas.flashmaster.view.components.CategoryChipsRow
import com.upchiapas.flashmaster.view.components.FlashcardDisplayCard
import com.upchiapas.flashmaster.view.components.ProgressOverviewCard
import com.upchiapas.flashmaster.viewmodel.FlashcardViewModel
import kotlinx.coroutines.launch

// pantalla principal que junta los composables y escucha el viewmodel mediante su factory
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardPage(
    viewModel: FlashcardViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as FlashMasterApplication).flashcardModule.factoryFlashcard
    )
) {
    // observo el estado reactivo con ciclo de vida
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tarjetaActual = if (uiState.filteredCards.isNotEmpty() && uiState.currentCardIndex in uiState.filteredCards.indices) {
        uiState.filteredCards[uiState.currentCardIndex]
    } else {
        null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                onClick = { viewModel.openAddDialog() },
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
                // selector de temas o categorias
                CategoryChipsRow(
                    categories = uiState.availableCategories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            }

            item {
                // resumen del progreso con barrita
                ProgressOverviewCard(
                    masteryPercentage = uiState.masteryPercentage,
                    countMastered = uiState.countMastered,
                    countInProcess = uiState.countInProcess,
                    countToReview = uiState.countToReview,
                    onResetClick = {
                        viewModel.resetProgress()
                        scope.launch {
                            snackbarHostState.showSnackbar("🔄 Progreso de estudio restablecido")
                        }
                    }
                )
            }

            item {
                // tarjeta interactiva central
                FlashcardDisplayCard(
                    card = tarjetaActual,
                    isAnswerRevealed = uiState.isAnswerRevealed,
                    currentIndex = uiState.currentCardIndex,
                    totalCards = uiState.filteredCards.size,
                    onToggleReveal = { viewModel.toggleRevealAnswer() },
                    onMasterySelected = { viewModel.setMastery(it) },
                    onPreviousClick = { viewModel.previousCard() },
                    onNextClick = { viewModel.nextCard() }
                )
            }

            item {
                // espacio abajo para que el boton flotante no tape nada
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // dialogo para registrar una pregunta nueva
        AddCardDialog(
            isOpen = uiState.isAddDialogOpen,
            questionText = uiState.newQuestionInput,
            answerText = uiState.newAnswerInput,
            selectedCategory = uiState.newCategoryInput,
            availableCategories = uiState.availableCategories,
            errorMessage = uiState.inputErrorMessage,
            onQuestionChange = { viewModel.onQuestionChange(it) },
            onAnswerChange = { viewModel.onAnswerChange(it) },
            onCategoryChange = { viewModel.onCategoryChange(it) },
            onConfirm = {
                val guardado = viewModel.saveNewCard()
                if (guardado) {
                    scope.launch {
                        snackbarHostState.showSnackbar("✅ Tarjeta guardada en DataStore exitosamente")
                    }
                }
            },
            onDismiss = { viewModel.closeAddDialog() }
        )
    }
}
