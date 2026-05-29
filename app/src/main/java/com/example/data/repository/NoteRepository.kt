package com.example.data.repository

import android.content.Context
import com.example.data.local.NoteDao
import com.example.data.local.entities.Folder
import com.example.data.local.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class NoteRepository(
    private val context: Context,
    private val noteDao: NoteDao
) {
    // Expose flows directly for UI observing
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val pinnedNotes: Flow<List<Note>> = noteDao.getPinnedNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    val deletedNotes: Flow<List<Note>> = noteDao.getDeletedNotes()
    val allFolders: Flow<List<Folder>> = noteDao.getAllFolders()

    fun getNotesInFolder(folderId: Int): Flow<List<Note>> = noteDao.getNotesInFolder(folderId)

    fun searchNotes(query: String): Flow<List<Note>> {
        val searchQuery = "%$query%"
        return noteDao.searchNotes(searchQuery)
    }

    suspend fun getNoteById(id: Int): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)
    }

    suspend fun getFolderById(id: Int): Folder? = withContext(Dispatchers.IO) {
        noteDao.getFolderById(id)
    }

    suspend fun insertNote(note: Note): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun insertFolder(folder: Folder): Long = withContext(Dispatchers.IO) {
        noteDao.insertFolder(folder)
    }

    suspend fun updateFolder(folder: Folder) = withContext(Dispatchers.IO) {
        noteDao.updateFolder(folder)
    }

    suspend fun deleteFolder(folder: Folder) = withContext(Dispatchers.IO) {
        noteDao.deleteFolder(folder)
    }

    // Move note to Recycle Bin (Trash)
    suspend fun moveNoteToTrash(noteId: Int) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            val updated = note.copy(
                isDeleted = true,
                deletedAt = System.currentTimeMillis(),
                isPinned = false // unpin automatically on deletion
            )
            noteDao.updateNote(updated)
        }
    }

    // Restore note from Recycle Bin (Trash)
    suspend fun restoreNoteFromTrash(noteId: Int) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            val updated = note.copy(
                isDeleted = false,
                deletedAt = null,
                dateModified = System.currentTimeMillis()
            )
            noteDao.updateNote(updated)
        }
    }

    // Pin/unpin a note
    suspend fun togglePinNote(noteId: Int) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            val updated = note.copy(
                isPinned = !note.isPinned,
                dateModified = System.currentTimeMillis()
            )
            noteDao.updateNote(updated)
        }
    }

    // Archive/unarchive a note
    suspend fun toggleArchiveNote(noteId: Int) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            val updated = note.copy(
                isArchived = !note.isArchived,
                isPinned = false, // unpin archived notes
                dateModified = System.currentTimeMillis()
            )
            noteDao.updateNote(updated)
        }
    }

    // Automated 30-day trash purge (EU GDPR retention minimization)
    suspend fun autoPurgeOldTrash() = withContext(Dispatchers.IO) {
        val threshold = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        noteDao.purgeTrash(threshold)
    }

    // GDPR Backup and Export ZIP Bundle
    suspend fun exportDataZip(): File = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "internote_export_${sdf.format(Date())}.zip"
        val exportFile = File(context.cacheDir, fileName)

        val notes = noteDao.getAllNotesDirect()
        val folders = noteDao.getAllFoldersDirect()

        val folderMap = folders.associateBy { it.id }

        ZipOutputStream(FileOutputStream(exportFile)).use { zos ->
            // JSON metadata serialization
            val jsonObj = JSONObject()
            val notesArray = JSONArray()
            val foldersArray = JSONArray()

            folders.forEach { folder ->
                val folderObj = JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("colorHex", folder.colorHex)
                    put("parentFolderId", folder.parentFolderId ?: JSONObject.NULL)
                }
                foldersArray.put(folderObj)
            }

            notes.forEach { note ->
                val noteObj = JSONObject().apply {
                    put("id", note.id)
                    put("title", note.title)
                    put("content", note.content)
                    put("folderId", note.folderId ?: JSONObject.NULL)
                    put("colorTag", note.colorTag ?: JSONObject.NULL)
                    put("isPinned", note.isPinned)
                    put("isArchived", note.isArchived)
                    put("isDeleted", note.isDeleted)
                    put("dateCreated", note.dateCreated)
                    put("dateModified", note.dateModified)
                }
                notesArray.put(noteObj)

                // Render individual Markdown file inside ZIP
                val sanitizedTitle = note.title.replace(Regex("[\\/\\\\\\?\\%\\*\\:\\|\\\"\\<\\>]"), "_").trim()
                val mdName = if (sanitizedTitle.isEmpty()) "Untitled_Note_${note.id}.md" else "$sanitizedTitle.md"
                
                // Nest markdown file under the folder matching its DB designation
                val folderName = note.folderId?.let { folderMap[it]?.name } ?: ""
                val entryPath = if (folderName.isNotEmpty()) {
                    "${folderName.replace(Regex("[\\/\\\\\\?\\%\\*\\:\\|\\\"\\<\\>]"), "_")}/$mdName"
                } else {
                    mdName
                }

                zos.putNextEntry(ZipEntry(entryPath))
                val mdContent = buildString {
                    append("# ").append(note.title).append("\n\n")
                    append("---").append("\n")
                    append("Created: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(note.dateCreated))).append("\n")
                    append("Modified: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(note.dateModified))).append("\n")
                    if (note.colorTag != null) {
                        append("Tag Color: ").append(note.colorTag).append("\n")
                    }
                    append("---").append("\n\n")
                    append(note.content)
                }
                zos.write(mdContent.toByteArray())
                zos.closeEntry()
            }

            jsonObj.put("export_title", "Inter Note App Export")
            jsonObj.put("exported_at", System.currentTimeMillis())
            jsonObj.put("notes", notesArray)
            jsonObj.put("folders", foldersArray)

            // Write metadata configuration
            zos.putNextEntry(ZipEntry("structure.json"))
            zos.write(jsonObj.toString(4).toByteArray())
            zos.closeEntry()
        }
        exportFile
    }

    // GDPR Right to Be Forgotten: Purges everything completely
    suspend fun purgeAllDataAndReset() = withContext(Dispatchers.IO) {
        // Clear database
        noteDao.clearAllNotes()
        noteDao.clearAllFolders()

        // Purge secure crypt configuration keys
        val cryptPrefs = context.getSharedPreferences("secure_crypt_prefs", Context.MODE_PRIVATE)
        cryptPrefs.edit().clear().commit()

        val vaultPrefs = context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)
        vaultPrefs.edit().clear().commit()

        val themePrefs = context.getSharedPreferences("inter_note_prefs", Context.MODE_PRIVATE)
        themePrefs.edit().clear().commit()

        // Overwrite and strip caches entirely
        context.cacheDir.deleteRecursively()
        context.filesDir.deleteRecursively()
    }

    // Auto-seed database with everyday normal user utility notes when empty
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingNotes = noteDao.getAllNotesDirect()
        val existingFolders = noteDao.getAllFoldersDirect()

        if (existingNotes.isEmpty() && existingFolders.isEmpty()) {
            val personalFolderId = noteDao.insertFolder(Folder(name = "Personal", colorHex = "#EC407A"))
            val ideasFolderId = noteDao.insertFolder(Folder(name = "Ideas", colorHex = "#FFA726"))
            val householdFolderId = noteDao.insertFolder(Folder(name = "Household", colorHex = "#66BB6A"))

            val now = System.currentTimeMillis()

            // Note 1: Standard Everyday Shopping List (Normal utility)
            noteDao.insertNote(Note(
                title = "Weekly Grocery List",
                content = "- Fresh fruits (bananas, apples, organic berries)\n- Whole wheat bread & local honey\n- Fresh vegetables (spinach, avocados, bell peppers)\n- Milk, Greek yogurt & block cheese\n- Ground coffee beans (medium roast)",
                folderId = householdFolderId.toInt(),
                colorTag = "#66BB6A",
                isPinned = true,
                dateCreated = now - 3600000 * 2, // 2 hours ago
                dateModified = now - 3600000 * 2
            ))

            // Note 2: Daily Reminders & Todo list
            noteDao.insertNote(Note(
                title = "Today's Simple Goals",
                content = "1. Morning stretch & 15-minute quick meditation\n2. Walk 10,000 steps around the neighborhood park\n3. Call Mom & catch up on the weekend details\n4. Drink at least 3 liters of water throughout the day\n5. Read 20 pages of currently checked-out library book",
                folderId = personalFolderId.toInt(),
                colorTag = "#EC407A",
                isPinned = true,
                dateCreated = now - 3600000, // 1 hour ago
                dateModified = now - 3600000
            ))

            // Note 3: Creative Ideas
            noteDao.insertNote(Note(
                title = "Gift Ideas for Holidays",
                content = "- Leather passport holder for brother's travel plans\n- Ceramic coffee mug set from the local farmers market art stall\n- Cozy wool scarf for sister (emerald green/navy colors)\n- Sound bar for dad's living room setup\n- Board game for family game nights (look into Ticket to Ride expansion)",
                folderId = ideasFolderId.toInt(),
                colorTag = "#FFA726",
                isPinned = false,
                dateCreated = now - 86400000, // 1 day ago
                dateModified = now - 86400000
            ))

            // Note 4: Recipe Note
            noteDao.insertNote(Note(
                title = "Quick Pancake Recipe 🥞",
                content = "### Ingredients:\n- 1 cup All-purpose flour\n- 2 tbsp Sugar\n- 2 tsp Baking powder\n- 1/2 tsp Salt\n- 1 cup Milk\n- 2 tbsp Butter (melted)\n- 1 Egg\n\n### Instructions:\n1. Mix dry ingredients in a large bowl.\n2. Whisk wet ingredients, then combine until mostly smooth.\n3. Heat a non-stick griddle over medium heat.\n4. Pour batter, flip when bubbles form on the surface, and cook until golden brown.",
                folderId = householdFolderId.toInt(),
                colorTag = "#66BB6A",
                isPinned = false,
                dateCreated = now - 86400000 * 2, // 2 days ago
                dateModified = now - 86400000 * 2
            ))
        }
    }
}
