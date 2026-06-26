package com.dinsoft.notes.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinsoft.notes.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = NoteDatabase.get(app).noteDao()
    private val aDao = NoteDatabase.get(app).attachmentDao()
    private val storage = Storage(app)

    val notes: StateFlow<List<Note>> = dao.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var lang by mutableStateOf("en")

    fun setLang(l: String) {
        lang = l
        val loc = Locale(l); Locale.setDefault(loc)
        getApplication<Application>().resources.configuration.setLocale(loc)
    }

    fun save(note: Note, atts: List<Attachment>) = viewModelScope.launch {
        val id = if (note.id == 0) dao.insert(note).toInt() else { dao.update(note); note.id }
        aDao.deleteByNoteId(id)
        atts.forEach { a -> aDao.insert(a.copy(id = 0, noteId = id)) }
        storage.saveNote(Note(id, note.title, note.content), atts)
        Toast.makeText(getApplication(), "✅ Tersimpan!", Toast.LENGTH_SHORT).show()
    }

    fun delete(note: Note) = viewModelScope.launch {
        aDao.deleteByNoteId(note.id); dao.delete(note); storage.deleteNote(note.id)
    }

    fun deleteAll() = viewModelScope.launch { dao.deleteAll() }
    fun attachments(noteId: Int) = aDao.getByNoteId(noteId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    fun backup(uri: Uri) = viewModelScope.launch { /* Backup logic */ }
    fun restore(uri: Uri) = viewModelScope.launch { /* Restore logic */ }
    fun backupName() = storage.backupName()
}