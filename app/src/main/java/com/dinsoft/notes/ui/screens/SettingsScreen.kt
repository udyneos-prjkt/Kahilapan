package com.dinsoft.notes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.R
import com.dinsoft.notes.ui.component.BackupDialog
import com.dinsoft.notes.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: NoteViewModel) {
    var backup by remember { mutableStateOf(false) }
    var about by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) {
        Column(Modifier.padding(16.dp)) {
            TextButton({ backup = true }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Backup, null); Text(stringResource(R.string.backup_restore)) }
            TextButton({ about = true }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Info, null); Text(stringResource(R.string.about_developer)) }
        }
    }
    if (backup) BackupDialog(vm) { backup = false }
    if (about) AboutDialog { about = false }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Kahilapan") },
        text = { Text("Notes App v1.0\nDeveloped by DinSoft\n© 2026") },
        confirmButton = { TextButton(onDismiss) { Text("OK") } })
}