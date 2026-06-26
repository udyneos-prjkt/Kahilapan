package com.dinsoft.notes.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "attachments",
    foreignKeys = [ForeignKey(entity = Note::class, parentColumns = ["id"], childColumns = ["noteId"], onDelete = ForeignKey.CASCADE)])
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int, val uri: String, val fileName: String,
    val type: String, val timestamp: Long = System.currentTimeMillis()
)