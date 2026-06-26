package com.dinsoft.notes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Attachment::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile private var db: NoteDatabase? = null
        fun get(context: Context) = db ?: synchronized(this) {
            Room.databaseBuilder(context, NoteDatabase::class.java, "notes.db")
                .fallbackToDestructiveMigration().build().also { db = it }
        }
    }
}