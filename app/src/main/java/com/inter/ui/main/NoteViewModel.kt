package com.inter.ui.main

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inter.data.local.entities.Folder
import com.inter.data.local.entities.Note
import com.inter.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class SortOrder {
    MODIFIED_DESC,
    MODIFIED_ASC,
    CREATED_DESC,
    CREATED_ASC,
    TITLE_ASC
}

enum class NotesFilterMode {
    ALL,
    ARCHIVE,
    TRASH
}

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val searchResults: List<Note> = emptyList(),
    val currentFilterMode: NotesFilterMode = NotesFilterMode.ALL,
    val selectedFolderId: Int? = null,
    val selectedColorTag: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.MODIFIED_DESC,
    val isGridLayout: Boolean = true
)

class NoteViewModel(
    application: Application,
    private val repository: NoteRepository
) : AndroidViewModel(application) {

    private val _currentFilterMode = MutableStateFlow(NotesFilterMode.ALL)
    private val _selectedFolderId = MutableStateFlow<Int?>(null)
    private val _selectedColorTag = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.MODIFIED_DESC)
    private val _isGridLayout = MutableStateFlow(true)

    // Events
    val exportEvent = MutableStateFlow<Uri?>(null)
    val resetAppEvent = MutableStateFlow(false)

    init {
        // Run automated 30-day trash retention check on model initiation and seed if empty
        viewModelScope.launch {
            try {
                repository.autoPurgeOldTrash()
                repository.seedDatabaseIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Expose basic streams from repository
    val folders: StateFlow<List<Folder>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashNotes: StateFlow<List<Note>> = repository.deletedNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedNotes: StateFlow<List<Note>> = repository.archivedNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined UI notes stream with sorting, filters and search criteria
    val uiState: StateFlow<NotesUiState> = combine(
        repository.activeNotes,
        folders,
        _currentFilterMode,
        _selectedFolderId,
        _selectedColorTag,
        _searchQuery,
        _sortOrder,
        _isGridLayout
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val active = array[0] as List<Note>
        @Suppress("UNCHECKED_CAST")
        val fldrs = array[1] as List<Folder>
        val mode = array[2] as NotesFilterMode
        val folderId = array[3] as Int?
        val colorTag = array[4] as String?
        val query = array[5] as String
        val sort = array[6] as SortOrder
        val grid = array[7] as Boolean
        
        // Filter by main display modes
        var list = when (mode) {
            NotesFilterMode.ALL -> active
            NotesFilterMode.ARCHIVE -> emptyList() // Archived and Trash are kept separate
            NotesFilterMode.TRASH -> emptyList()
        }

        // Apply Folder specification
        if (folderId != null) {
            list = list.filter { it.folderId == folderId }
        }

        // Apply Tag selection
        if (colorTag != null) {
            list = list.filter { it.colorTag == colorTag }
        }

        // Apply textual Search matching
        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.content.contains(query, ignoreCase = true) ||
                (it.colorTag?.contains(query, ignoreCase = true) == true)
            }
        }

        // Arrange in specified sorting logic
        list = when (sort) {
            SortOrder.MODIFIED_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.dateModified })
            SortOrder.MODIFIED_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.dateModified })
            SortOrder.CREATED_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.dateCreated })
            SortOrder.CREATED_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.dateCreated })
            SortOrder.TITLE_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() })
        }

        NotesUiState(
            notes = list,
            folders = fldrs,
            currentFilterMode = mode,
            selectedFolderId = folderId,
            selectedColorTag = colorTag,
            searchQuery = query,
            sortOrder = sort,
            isGridLayout = grid
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotesUiState())

    // Note operations
    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }

    suspend fun createNote(title: String, content: String, folderId: Int?, colorTag: String?): Int {
        val newNote = Note(
            title = title,
            content = content,
            folderId = folderId,
            colorTag = colorTag,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
        val newId = repository.insertNote(newNote)
        return newId.toInt()
    }

    fun createNoteAsync(title: String, content: String, folderId: Int?, colorTag: String?) {
        viewModelScope.launch {
            createNote(title, content, folderId, colorTag)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(dateModified = System.currentTimeMillis()))
        }
    }

    fun togglePin(noteId: Int) {
        viewModelScope.launch {
            repository.togglePinNote(noteId)
        }
    }

    fun toggleArchive(noteId: Int) {
        viewModelScope.launch {
            repository.toggleArchiveNote(noteId)
        }
    }

    fun deleteNoteToTrash(noteId: Int) {
        viewModelScope.launch {
            repository.moveNoteToTrash(noteId)
        }
    }

    fun restoreFromTrash(noteId: Int) {
        viewModelScope.launch {
            repository.restoreNoteFromTrash(noteId)
        }
    }

    fun permanentlyDeleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Folder operations
    fun createFolder(name: String, colorHex: String, parentId: Int? = null) {
        viewModelScope.launch {
            val newFolder = Folder(
                name = name,
                colorHex = colorHex,
                parentFolderId = parentId
            )
            repository.insertFolder(newFolder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
            // Dissolve notes out of deleted folder
            // Handled safely by letting note folderId remain orphaned or updated in DAO.
        }
    }

    // Filters and UI Toggles
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFolder(folderId: Int?) {
        _selectedFolderId.value = folderId
    }

    fun selectColorTag(colorTag: String?) {
        _selectedColorTag.value = colorTag
    }

    fun updateSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleLayoutMode() {
        _isGridLayout.value = !_isGridLayout.value
    }

    fun setFilterMode(mode: NotesFilterMode) {
        _currentFilterMode.value = mode
        if (mode != NotesFilterMode.ALL) {
            _selectedFolderId.value = null // reset folders filters on other tabs
        }
    }

    // GDPR Export Utilities
    fun performExport() {
        viewModelScope.launch {
            try {
                val zipFile = repository.exportDataZip()
                val authority = "${getApplication<Application>().packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(getApplication(), authority, zipFile)
                exportEvent.value = uri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearExportEvent() {
        exportEvent.value = null
    }

    // GDPR Right to Be Forgotten
    fun wipeAllUserData() {
        viewModelScope.launch {
            repository.purgeAllDataAndReset()
            resetAppEvent.value = true
        }
    }
}
