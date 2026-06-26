// app/src/main/java/com/dinsoft/notes/data/Attachment.kt
package com.dinsoft.notes.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int,
    val uri: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val type: AttachmentType,
    val timestamp: Long = System.currentTimeMillis()
)