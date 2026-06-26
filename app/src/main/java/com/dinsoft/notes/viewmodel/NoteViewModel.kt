// app/src/main/java/com/dinsoft/notes/viewmodel/NoteViewModel.kt
package com.dinsoft.notes.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinsoft.notes.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NoteDatabase.getDatabase(application)
    private val dao = database.noteDao()
    private val attachmentDao = database.attachmentDao()
    private val backupManager = BackupRestoreManager(application)
    
    val notes: StateFlow<List<Note>> = dao.getAllNotes()
        .let { flow ->
            val stateFlow = MutableStateFlow<List<Note>>(emptyList())
            viewModelScope.launch {
                flow.collect { stateFlow.value = it }
            }
            stateFlow.asStateFlow()
        }
    
    // Language support
    var currentLanguage by mutableStateOf("en")
    
    fun setLanguage(language: String) {
        currentLanguage = language
        val context = getApplication<Application>()
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    // Note functions
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
    
    // Attachment functions
    fun saveAttachment(attachment: Attachment) {
        viewModelScope.launch {
            attachmentDao.insertAttachment(attachment)
        }
    }
    
    fun getAttachmentsForNote(noteId: Int): StateFlow<List<Attachment>> {
        return attachmentDao.getAttachmentsForNote(noteId)
            .let { flow ->
                val stateFlow = MutableStateFlow<List<Attachment>>(emptyList())
                viewModelScope.launch {
                    flow.collect { stateFlow.value = it }
                }
                stateFlow.asStateFlow()
            }
    }
    
    fun deleteAttachment(attachment: Attachment) {
        viewModelScope.launch {
            attachmentDao.deleteAttachment(attachment)
        }
    }
    
    // Backup functions
    fun backupNotes(uri: Uri) {
        viewModelScope.launch {
            backupManager.backupNotes(uri)
        }
    }
    
    fun restoreNotes(uri: Uri) {
        viewModelScope.launch {
            backupManager.restoreNotes(uri)
        }
    }
    
    fun getBackupFileName(): String {
        return backupManager.generateBackupFileName()
    }
}