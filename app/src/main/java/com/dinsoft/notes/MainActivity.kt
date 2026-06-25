// app/src/main/java/com/dinsoft/notes/MainActivity.kt
package com.dinsoft.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dinsoft.notes.ui.screens.NoteScreen
import com.dinsoft.notes.ui.Theme.NotesTheme
import com.dinsoft.notes.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NoteScreen(viewModel = viewModel)
                }
            }
        }
    }
}