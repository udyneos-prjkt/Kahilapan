// app/src/main/java/com/dinsoft/notes/MainActivity.kt
package com.dinsoft.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.dinsoft.notes.ui.screens.NoteScreen
import com.dinsoft.notes.ui.screens.SplashScreen
import com.dinsoft.notes.ui.theme.NotesTheme
import com.dinsoft.notes.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ubah warna status bar
        window.statusBarColor = android.graphics.Color.parseColor("#1A1A2E") // Warna gelap
        // Atau bisa juga:
        // window.statusBarColor = getColor(R.color.splash_background)
        
        // Membuat ikon status bar putih (untuk background gelap)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false // false = ikon putih, true = ikon hitam
        }
        
        setContent {
            NotesTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    // Warna status bar untuk splash screen
                    SideEffect {
                        window.statusBarColor = android.graphics.Color.parseColor("#1A1A2E")
                        WindowCompat.getInsetsController(window, window.decorView).apply {
                            isAppearanceLightStatusBars = false
                        }
                    }
                    
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                } else {
                    // Warna status bar untuk main screen
                    SideEffect {
                        window.statusBarColor = android.graphics.Color.parseColor("#121212")
                        WindowCompat.getInsetsController(window, window.decorView).apply {
                            isAppearanceLightStatusBars = false
                        }
                    }
                    
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NoteScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}