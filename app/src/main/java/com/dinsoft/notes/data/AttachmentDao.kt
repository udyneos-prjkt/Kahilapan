// app/src/main/java/com/dinsoft/notes/data/AttachmentDao.kt
package com.dinsoft.notes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY timestamp DESC")
    fun getAttachmentsForNote(noteId: Int): Flow<List<Attachment>>
    
    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY timestamp DESC")
    suspend fun getAttachmentsForNoteOnce(noteId: Int): List<Attachment>
    
    @Query("SELECT * FROM attachments WHERE noteId = :noteId AND type = :type")
    fun getAttachmentsByType(noteId: Int, type: AttachmentType): Flow<List<Attachment>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: Attachment)
    
    @Delete
    suspend fun deleteAttachment(attachment: Attachment)
    
    @Query("DELETE FROM attachments WHERE noteId = :noteId")
    suspend fun deleteAttachmentsForNote(noteId: Int)
    
    @Query("SELECT COUNT(*) FROM attachments WHERE noteId = :noteId")
    suspend fun getAttachmentCount(noteId: Int): Int
}