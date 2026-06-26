package com.dinsoft.notes.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Storage(private val ctx: Context) {
    private val gson = Gson()
    private val dir get() = ctx.getExternalFilesDir("data")!!.also { it.mkdirs() }

    fun saveNote(note: Note, atts: List<Attachment>) {
        File(dir, "note_${note.id}").writeText(Crypto.enc(gson.toJson(BackupNote(note, atts))))
    }

    fun readNote(id: Int) = try {
        val json = Crypto.dec(File(dir, "note_$id").readText())
        gson.fromJson(json, BackupNote::class.java)
    } catch (e: Exception) { null }

    fun saveFile(name: String, data: ByteArray): Uri {
        File(dir, name).writeBytes(data)
        return Uri.fromFile(File(dir, name))
    }

    fun readFile(name: String) = File(dir, name).takeIf { it.exists() }?.readBytes()
    fun deleteNote(id: Int) { File(dir, "note_$id").delete() }
    fun backupName() = "Kahilapan_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.zip"
}

data class BackupNote(val note: Note, val attachments: List<Attachment>)