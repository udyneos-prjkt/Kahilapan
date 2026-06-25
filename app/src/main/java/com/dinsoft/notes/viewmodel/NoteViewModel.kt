// app/src/main/java/com/dinsoft/notes/viewmodel/NoteViewModel.kt
package com.dinsoft.notes.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinsoft.notes.data.BackupRestoreManager
import com.dinsoft.notes.data.Note
import com.dinsoft.notes.data.NoteDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = NoteDatabase.getDatabase(application).noteDao()
    private val backupManager = BackupRestoreManager(application)
    
    val notes: StateFlow<List<Note>> = dao.getAllNotes()
        .let { flow ->
            val stateFlow = MutableStateFlow<List<Note>>(emptyList())
            viewModelScope.launch {
                flow.collect { stateFlow.value = it }
            }
            stateFlow.asStateFlow()
        }
    
    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (note.id == 0) {
                dao.insertNote(note)
            } else {
                dao.updateNote(note)
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }
    
    fun deleteAllNotes() {
        viewModelScope.launch {
            dao.deleteAllNotes()
        }
    }
    
    // Fungsi Backup
    fun backupNotes(uri: Uri) {
        viewModelScope.launch {
            backupManager.backupNotes(uri)
        }
    }
    
    // Fungsi Restore
    fun restoreNotes(uri: Uri) {
        viewModelScope.launch {
            backupManager.restoreNotes(uri)
        }
    }
    
    // Generate nama file backup
    fun getBackupFileName(): String {
        return backupManager.generateBackupFileName()
    }
}