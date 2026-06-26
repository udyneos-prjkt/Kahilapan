// app/src/main/java/com/dinsoft/notes/data/BackupRestoreManager.kt
package com.dinsoft.notes.data

import android.content.Context
import android.net.Uri
import android.util.Base64
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
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class BackupRestoreManager(private val context: Context) {
    
    private val gson = Gson()
    private val dao = NoteDatabase.getDatabase(context).noteDao()
    private val attachmentDao = NoteDatabase.getDatabase(context).attachmentDao()
    private val secretKey = "Kahilapan2026Key" // 16 bytes for AES
    
    // Backup sebagai ZIP dengan JSON terenkripsi Base64
    suspend fun backupNotes(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val notes = dao.getAllNotesOnce()
                val backupData = mutableListOf<BackupNote>()
                
                notes.forEach { note ->
                    val attachments = attachmentDao.getAttachmentsForNoteOnce(note.id)
                    // Konversi URI ke path relatif
                    val updatedAttachments = attachments.map { att ->
                        att.copy(uri = extractFileName(att.uri))
                    }
                    backupData.add(BackupNote(note = note, attachments = updatedAttachments))
                }
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                        
                        // 1. JSON metadata (Encrypted Base64)
                        zipOut.putNextEntry(ZipEntry("notes.json"))
                        val json = gson.toJson(backupData)
                        val encryptedJson = encryptBase64(json)  // ← Encrypt
                        zipOut.write(encryptedJson.toByteArray())
                        zipOut.closeEntry()
                        
                        // 2. File attachments
                        notes.forEach { note ->
                            val attachments = attachmentDao.getAttachmentsForNoteOnce(note.id)
                            attachments.forEach { attachment ->
                                try {
                                    val attachmentUri = Uri.parse(attachment.uri)
                                    // Hanya backup jika file ada di internal storage
                                    if (attachment.uri.startsWith("file://") || attachment.uri.startsWith("/")) {
                                        val file = File(attachmentUri.path ?: return@forEach)
                                        if (file.exists()) {
                                            zipOut.putNextEntry(ZipEntry("attachments/${attachment.fileName}"))
                                            file.inputStream().use { it.copyTo(zipOut) }
                                            zipOut.closeEntry()
                                        }
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
                
                ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        when {
                            entry.name == "notes.json" -> {
                                val encryptedJson = zipIn.bufferedReader().readText()
                                jsonData = decryptBase64(encryptedJson)  // ← Decrypt
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
                        val note = backupNote.note.copy(id = 0, timestamp = System.currentTimeMillis())
                        val noteId = dao.insertNote(note).toInt()
                        
                        // Restore attachments
                        backupNote.attachments?.forEach { attachment ->
                            val savedUri = saveAttachmentFile(attachment.fileName, attachmentFiles[attachment.fileName])
                            
                            if (savedUri != null) {
                                attachmentDao.insertAttachment(
                                    attachment.copy(
                                        id = 0,
                                        noteId = noteId,
                                        uri = savedUri.toString()
                                    )
                                )
                            }
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
    
    private fun saveAttachmentFile(fileName: String, data: ByteArray?): Uri? {
        if (data == null) return null
        try {
            val dir = File(context.filesDir, "attachments")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.writeBytes(data)
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    // Encrypt Base64
    private fun encryptBase64(text: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(text.toByteArray())
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback: plain base64
            Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP)
        }
    }
    
    // Decrypt Base64
    private fun decryptBase64(encryptedText: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(encryptedText, Base64.NO_WRAP)
            String(cipher.doFinal(decoded))
        } catch (e: Exception) {
            // Fallback: plain base64 decode
            String(Base64.decode(encryptedText, Base64.NO_WRAP))
        }
    }
    
    private fun extractFileName(uri: String): String {
        return uri.substringAfterLast("/")
    }
    
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "Kahilapan_Backup_${dateFormat.format(Date())}"
    }
}

data class BackupNote(
    val note: Note,
    val attachments: List<Attachment>? = null
)