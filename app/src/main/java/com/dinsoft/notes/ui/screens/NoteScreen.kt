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
import com.dinsoft.notes.ui.Component.BackupRestoreDialog
import com.dinsoft.notes.ui.Component.NoteCard
import com.dinsoft.notes.ui.Component.NoteDialog
import com.dinsoft.notes.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(viewModel: NoteViewModel) {
    val notesState by viewModel.notes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NoteAlt,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📝 Notes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Backup & Restore Button
                    IconButton(onClick = { showBackupDialog = true }) {
                        Icon(Icons.Default.Backup, "Backup & Restore")
                    }
                    
                    // Menu Dropdown
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Backup & Restore") },
                                onClick = {
                                    showMenu = false
                                    showBackupDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Backup, null)
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Delete All Notes") },
                                onClick = {
                                    showMenu = false
                                    noteToDelete = null
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
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
    
    // Note Dialog
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
    
    // Delete Confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (noteToDelete != null) "Delete Note" else "Delete All Notes") },
            text = { 
                Text(
                    if (noteToDelete != null) 
                        "Are you sure you want to delete this note?" 
                    else 
                        "Are you sure you want to delete ALL notes? This action cannot be undone!"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (noteToDelete != null) {
                            viewModel.deleteNote(noteToDelete!!)
                        } else {
                            viewModel.deleteAllNotes()
                        }
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
    
    // Backup Restore Dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false }
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