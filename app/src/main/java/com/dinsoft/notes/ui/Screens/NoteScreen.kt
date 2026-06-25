// app/src/main/java/com/dinsoft/notes/ui/screens/NoteScreen.kt
package com.dinsoft.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.data.Note
import com.dinsoft.notes.ui.Component.NoteCard
import com.dinsoft.notes.ui.Component.NoteDialog
import com.dinsoft.notes.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)  // ← TAMBAHKAN INI
@Composable
fun NoteScreen(viewModel: NoteViewModel) {
    val notesState by viewModel.notes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 Notes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedNote = null
                    showDialog = true
                },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("New Note") }
            )
        }
    ) { padding ->
        if (notesState.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notesState, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = {
                            selectedNote = note
                            showDialog = true
                        },
                        onDelete = {
                            noteToDelete = note
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    
    if (showDialog) {
        NoteDialog(
            note = selectedNote,
            onDismiss = { showDialog = false },
            onSave = { note ->
                viewModel.saveNote(note)
                showDialog = false
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.NoteAdd,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No notes yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap + to create your first note",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}