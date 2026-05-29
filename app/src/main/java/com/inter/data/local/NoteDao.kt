package com.inter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.inter.data.local.entities.Folder
import com.inter.data.local.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // Note operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, dateModified DESC")
    fun getActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND isPinned = 1 ORDER BY dateModified DESC")
    fun getPinnedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 1 ORDER BY dateModified DESC")
    fun getArchivedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND folderId = :folderId ORDER BY isPinned DESC, dateModified DESC")
    fun getNotesInFolder(folderId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND (title LIKE :query OR content LIKE :query OR colorTag LIKE :query) ORDER BY isPinned DESC, dateModified DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesDirect(): List<Note>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, dateModified DESC")
    fun getActiveNotesDirect(): List<Note>

    @Query("SELECT * FROM folders")
    suspend fun getAllFoldersDirect(): List<Folder>

    // Folder operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: Int): Folder?

    // Maintenance and GDPR
    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :purgeThreshold")
    suspend fun purgeTrash(purgeThreshold: Long)

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()

    @Query("DELETE FROM folders")
    suspend fun clearAllFolders()
}
