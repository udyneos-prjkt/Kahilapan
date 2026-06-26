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
            viewModelScope.launch { flow.collect { stateFlow.value = it } }
            stateFlow.asStateFlow()
        }
    
    var currentLanguage by mutableStateOf("en")
        private set
    
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
    
    // Save note dengan attachments
    fun saveNoteWithAttachments(note: Note, attachments: List<Attachment>) {
        viewModelScope.launch {
            // Simpan note dulu
            if (note.id == 0) {
                dao.insertNote(note)
            } else {
                dao.updateNote(note)
                // Hapus attachments lama
                attachmentDao.deleteAttachmentsForNote(note.id)
            }
            
            // Simpan attachments dengan noteId yang benar
            attachments.forEach { attachment ->
                attachmentDao.insertAttachment(
                    attachment.copy(noteId = note.id)
                )
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            attachmentDao.deleteAttachmentsForNote(note.id)
            dao.deleteNote(note)
        }
    }
    
    fun deleteAllNotes() {
        viewModelScope.launch {
            dao.deleteAllNotes()
        }
    }
    
    fun getAttachmentsForNoteOnce(noteId: Int): List<Attachment> {
        // Untuk mengambil attachments saat edit
        var result = emptyList<Attachment>()
        viewModelScope.launch {
            result = attachmentDao.getAttachmentsForNoteOnce(noteId)
        }
        return result
    }
    
    fun backupNotes(uri: Uri) {
        viewModelScope.launch { backupManager.backupNotes(uri) }
    }
    
    fun restoreNotes(uri: Uri) {
        viewModelScope.launch { backupManager.restoreNotes(uri) }
    }
    
    fun getBackupFileName(): String = backupManager.generateBackupFileName()
}