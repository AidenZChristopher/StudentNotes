package com.example.studentnotes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.studentnotes.data.entity.Folder
import com.example.studentnotes.data.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // --- Folder Operations ---
    // Modified: Added search capability using LIKE
    @Query("SELECT * FROM folders WHERE name LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    fun getFolders(searchQuery: String): Flow<List<Folder>>

    @Insert
    suspend fun insertFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    // --- Note Operations ---
    // Modified: Added search capability filtering by Title OR Content within a specific folder
    @Query("""
        SELECT * FROM notes 
        WHERE folderId = :folderId 
        AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%') 
        ORDER BY date DESC
    """)
    fun getNotesByFolder(folderId: Int, searchQuery: String): Flow<List<Note>>

    @Insert
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}
