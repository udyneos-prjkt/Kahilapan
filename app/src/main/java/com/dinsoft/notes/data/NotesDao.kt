// app/src/main/java/com/dinsoft/notes/data/NoteDao.kt
package com.dinsoft.notes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    suspend fun getAllNotesOnce(): List<Note>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long  // ← HARUS RETURN Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
    
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int
}