package com.example.studentnotes.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.studentnotes.data.dao.NoteDao
import com.example.studentnotes.data.entity.Folder
import com.example.studentnotes.data.entity.Note

@Database(entities = [Note::class, Folder::class], version = 2)
abstract class NoteDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
