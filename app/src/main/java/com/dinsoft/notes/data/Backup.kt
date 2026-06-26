// app/src/main/java/com/dinsoft/notes/data/BackupRestoreManager.kt
package com.dinsoft.notes.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dinsoft.notes.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupRestoreManager(private val context: Context) {
    
    private val gson = Gson()
    private val dao = NoteDatabase.getDatabase(context).noteDao()
    private val attachmentDao = NoteDatabase.getDatabase(context).attachmentDao()
    
    // Backup sebagai ZIP (JSON + files)
    suspend fun backupNotes(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val notes = dao.getAllNotesOnce()
                val backupData = mutableListOf<BackupNote>()
                
                notes.forEach { note ->
                    val attachments = attachmentDao.getAttachmentsForNoteOnce(note.id)
                    backupData.add(BackupNote(note = note, attachments = attachments))
                }
                
                // Buat ZIP
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                        // 1. Masukkan JSON metadata
                        zipOut.putNextEntry(ZipEntry("notes.json"))
                        val json = gson.toJson(backupData)
                        zipOut.write(json.toByteArray())
                        zipOut.closeEntry()
                        
                        // 2. Masukkan file attachments
                        backupData.forEach { backupNote ->
                            backupNote.attachments?.forEach { attachment ->
                                try {
                                    val attachmentUri = Uri.parse(attachment.uri)
                                    val inputStream = context.contentResolver.openInputStream(attachmentUri)
                                    inputStream?.use { input ->
                                        zipOut.putNextEntry(
                                            ZipEntry("attachments/${attachment.fileName}")
                                        )
                                        input.copyTo(zipOut)
                                        zipOut.closeEntry()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.backup_success), Toast.LENGTH_SHORT).show()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.backup_failed, e.message), Toast.LENGTH_LONG).show()
                }
                false
            }
        }
    }
    
    // Restore dari ZIP
    suspend fun restoreNotes(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
                
                var jsonData: String? = null
                val attachmentFiles = mutableMapOf<String, ByteArray>()
                
                // Baca ZIP
                ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        when {
                            entry.name == "notes.json" -> {
                                jsonData = zipIn.bufferedReader().readText()
                            }
                            entry.name.startsWith("attachments/") -> {
                                val fileName = entry.name.removePrefix("attachments/")
                                attachmentFiles[fileName] = zipIn.readBytes()
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
                
                if (jsonData != null) {
                    val type = object : TypeToken<List<BackupNote>>() {}.type
                    val backupData: List<BackupNote> = gson.fromJson(jsonData, type)
                    
                    // Hapus data lama
                    dao.deleteAllNotes()
                    
                    // Restore notes
                    backupData.forEach { backupNote ->
                        val note = backupNote.note.copy(
                            id = 0,
                            timestamp = System.currentTimeMillis()
                        )
                        dao.insertNote(note)
                        
                        // Restore attachments
                        backupNote.attachments?.forEach { attachment ->
                            // Simpan file ke internal storage
                            val savedUri = saveAttachmentFile(attachment.fileName, attachmentFiles[attachment.fileName])
                            
                            attachmentDao.insertAttachment(
                                attachment.copy(
                                    id = 0,
                                    noteId = note.id,
                                    uri = savedUri?.toString() ?: attachment.uri
                                )
                            )
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.restore_success, backupData.size),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.restore_failed, e.message), Toast.LENGTH_LONG).show()
                }
                false
            }
        }
    }
    
    // Simpan file attachment ke internal storage
    private fun saveAttachmentFile(fileName: String, data: ByteArray?): Uri? {
        if (data == null) return null
        
        try {
            val file = File(context.filesDir, "attachments/$fileName")
            file.parentFile?.mkdirs()
            file.writeBytes(data)
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "Kahilapan_Backup_${dateFormat.format(Date())}.zip"  // ← ZIP extension
    }
}

data class BackupNote(
    val note: Note,
    val attachments: List<Attachment>? = null
)