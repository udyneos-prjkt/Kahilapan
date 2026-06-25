// app/src/main/java/com/dinsoft/notes/MainActivity.kt
package com.dinsoft.notes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.dinsoft.notes.ui.screens.NoteScreen
import com.dinsoft.notes.ui.screens.SplashScreen
import com.dinsoft.notes.ui.theme.NotesTheme  // PASTIKAN folder "theme" lowercase
import com.dinsoft.notes.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Atur warna status bar
        window.statusBarColor = Color.parseColor("#0A0A1A")
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        
        setContent {
            NotesTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    LaunchedEffect(Unit) {
                        window.statusBarColor = Color.parseColor("#1A1A2E")
                    }
                    
                    SplashScreen(
                        onSplashFinished = {
                            window.statusBarColor = Color.parseColor("#0A0A1A")
                            showSplash = false
                        }
                    )
                } else {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NoteScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}