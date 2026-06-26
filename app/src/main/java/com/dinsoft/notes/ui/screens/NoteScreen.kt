package com.dinsoft.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.R
import com.dinsoft.notes.data.Note
import com.dinsoft.notes.ui.component.NoteCard
import com.dinsoft.notes.ui.component.NoteDialog
import com.dinsoft.notes.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(vm: NoteViewModel) {
    val notes by vm.notes.collectAsState()
    var dialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Note?>(null) }
    var settings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = { IconButton(onClick = { settings = true }) { Icon(Icons.Default.Settings, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { selected = null; dialog = true }) { Icon(Icons.Default.Add, null) }
        }
    ) { pad ->
        if (notes.isEmpty()) Empty(modifier = Modifier.padding(pad))
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes) { note ->
                NoteCard(note, onClick = { selected = note; dialog = true }, onDelete = { vm.delete(note) })
            }
        }
    }

    if (dialog) NoteDialog(selected, onDismiss = { dialog = false }, onSave = { n, a -> vm.save(n, a); dialog = false })
    if (settings) SettingsScreen(onBack = { settings = false }, vm = vm)
}

@Composable
fun Empty(modifier: Modifier) = Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(stringResource(R.string.no_notes), style = MaterialTheme.typography.headlineSmall)
}