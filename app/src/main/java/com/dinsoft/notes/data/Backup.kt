// app/src/main/java/com/dinsoft/notes/data/BackupRestoreManager.kt
package com.dinsoft.notes.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
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
    
    // Backup ke file JSON
    suspend fun backupNotes(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val notes = dao.getAllNotesOnce()
                val json = gson.toJson(notes)
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                    outputStream.flush()
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Backup berhasil!", Toast.LENGTH_SHORT).show()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Backup gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
                false
            }
        }
    }
    
    // Restore dari file JSON
    suspend fun restoreNotes(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader()?.readText() ?: return@withContext false
                inputStream.close()
                
                val type = object : TypeToken<List<Note>>() {}.type
                val notes: List<Note> = gson.fromJson(json, type)
                
                // Hapus semua notes lama dan insert yang baru
                dao.deleteAllNotes()
                notes.forEach { note ->
                    dao.insertNote(
                        note.copy(
                            id = 0, // Reset ID
                            timestamp = System.currentTimeMillis() // Update timestamp
                        )
                    )
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "✅ ${notes.size} catatan berhasil direstore!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Restore gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
                false
            }
        }
    }
    
    // Generate nama file backup
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "Notes_Backup_${dateFormat.format(Date())}.json"
    }
}