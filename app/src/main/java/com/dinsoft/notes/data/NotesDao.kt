package com.dinsoft.notes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC") fun getAll(): Flow<List<Note>>
    @Query("SELECT * FROM notes ORDER BY timestamp DESC") suspend fun getAllOnce(): List<Note>
    @Insert suspend fun insert(note: Note): Long
    @Update suspend fun update(note: Note)
    @Delete suspend fun delete(note: Note)
    @Query("DELETE FROM notes") suspend fun deleteAll()
}