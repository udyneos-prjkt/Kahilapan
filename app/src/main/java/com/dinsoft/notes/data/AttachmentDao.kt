package com.dinsoft.notes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY timestamp DESC")
    fun getByNoteId(noteId: Int): Flow<List<Attachment>>
    
    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY timestamp DESC")
    suspend fun getByNoteIdOnce(noteId: Int): List<Attachment>
    
    @Insert suspend fun insert(attachment: Attachment)
    @Query("DELETE FROM attachments WHERE noteId = :noteId") suspend fun deleteByNoteId(noteId: Int)
    @Query("SELECT COUNT(*) FROM attachments WHERE noteId = :noteId") suspend fun count(noteId: Int): Int
}