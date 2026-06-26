// app/src/main/java/com/dinsoft/notes/data/BackupRestoreManager.kt
package com.dinsoft.notes.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dinsoft.notes.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreManager(private val context: Context) {
    
    private val gson = Gson()
    private val dao = NoteDatabase.getDatabase(context).noteDao()
    private val attachmentDao = NoteDatabase.getDatabase(context).attachmentDao()
    
    // Backup notes dan attachments ke file JSON
    suspend fun backupNotes(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val notes = dao.getAllNotesOnce()
                val backupData = mutableListOf<BackupNote>()
                
                // Ambil attachments untuk setiap note
                notes.forEach { note ->
                    val attachments = attachmentDao.getAttachmentsForNoteOnce(note.id)
                    backupData.add(
                        BackupNote(
                            note = note,
                            attachments = attachments
                        )
                    )
                }
                
                val json = gson.toJson(backupData)
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                    outputStream.flush()
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.backup_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.backup_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
                false
            }
        }
    }
    
    // Restore notes dan attachments dari file JSON
    suspend fun restoreNotes(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader()?.readText() ?: return@withContext false
                inputStream.close()
                
                val type = object : TypeToken<List<BackupNote>>() {}.type
                val backupData: List<BackupNote> = gson.fromJson(json, type)
                
                // Hapus semua data lama
                dao.deleteAllNotes()
                
                // Restore notes dan attachments
                var restoredCount = 0
                backupData.forEach { backupNote ->
                    val note = backupNote.note.copy(
                        id = 0, // Reset ID
                        timestamp = System.currentTimeMillis() // Update timestamp
                    )
                    dao.insertNote(note)
                    
                    // Restore attachments jika ada
                    backupNote.attachments?.forEach { attachment ->
                        attachmentDao.insertAttachment(
                            attachment.copy(
                                id = 0,
                                noteId = note.id
                            )
                        )
                    }
                    restoredCount++
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.restore_success, restoredCount),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.restore_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
                false
            }
        }
    }
    
    // Generate nama file backup
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "Kahilapan_Backup_${dateFormat.format(Date())}.json"
    }
}

// Data class untuk backup (Note + Attachments)
data class BackupNote(
    val note: Note,
    val attachments: List<Attachment>? = null
)