package com.dinsoft.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dinsoft.notes.ui.screens.NoteScreen
import com.dinsoft.notes.ui.screens.SplashScreen
import com.dinsoft.notes.ui.theme.KahilapanTheme
import com.dinsoft.notes.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KahilapanTheme {
                var splash by remember { mutableStateOf(true) }
                if (splash) SplashScreen { splash = false }
                else Surface(Modifier.fillMaxSize()) { NoteScreen(viewModel = viewModels<NoteViewModel>().value) }
            }
        }
    }
}