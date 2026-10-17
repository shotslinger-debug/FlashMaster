package com.upchiapas.flashmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.upchiapas.flashmaster.ui.theme.FlashMasterTheme
import com.upchiapas.flashmaster.view.FlashcardPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashMasterTheme {
                FlashcardPage()
            }
        }
    }
}
