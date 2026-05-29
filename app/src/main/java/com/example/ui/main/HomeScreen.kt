package com.example.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.Folder
import com.example.data.local.entities.Note
import com.example.ui.editor.MarkdownRenderer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel,
    onNavigateToEditor: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()
    val archivedNotes by viewModel.archivedNotes.collectAsStateWithLifecycle()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedFolderColor by remember { mutableStateOf("#42A5F5") }

    val foldersList = uiState.folders

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inter Note",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleLayoutMode() },
                        modifier = Modifier.testTag("toggle_layout_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridLayout) Icons.Outlined.GridView else Icons.Outlined.ViewList,
                            contentDescription = "Toggle Grid/List Layout"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Open Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (uiState.currentFilterMode == NotesFilterMode.ALL) {
                ExtendedFloatingActionButton(
                    text = { Text("New Note", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Filled.Add, "New Note") },
                    onClick = { onNavigateToEditor(-1) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("create_note_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // High Density Live Search Bar
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("note_search_input"),
                placeholder = { Text("Search notes & folders", color = Color(0xFF44474E)) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search Icon", tint = Color(0xFF44474E).copy(alpha = 0.7f)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, "Clear Search")
                        }
                    } else {
                        // High Density Profile avatar "JD" indicator from mockup
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF005AC1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEEF0F6),
                    unfocusedContainerColor = Color(0xFFEEF0F6),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            // High Density GDPR Security status & Compliance banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD8E2FF))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF005AC1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Secured",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GDPR Secured & Local-First",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF001A41)
                    )
                    Text(
                        text = "All data encrypted with SQLCipher (EU Compliant)",
                        fontSize = 10.sp,
                        color = Color(0xFF001A41).copy(alpha = 0.73f),
                        lineHeight = 13.sp
                    )
                }
            }

            // Dynamic filter options row
            FilterCategoryTabs(
                currentMode = uiState.currentFilterMode,
                onModeSelected = { viewModel.setFilterMode(it) }
            )

            // Dynamic folder filtering strip (only visible in main tab)
            if (uiState.currentFilterMode == NotesFilterMode.ALL) {
                FolderSelectorRow(
                    folders = foldersList,
                    selectedFolderId = uiState.selectedFolderId,
                    onFolderSelected = { viewModel.selectFolder(it) },
                    onCreateFolderClick = { showCreateFolderDialog = true }
                )
            }

            // Tag Color filtering selection strip
            ColorTagFilterRow(
                selectedColor = uiState.selectedColorTag,
                onColorSelected = { viewModel.selectColorTag(it) }
            )

            // Notes representation view (Supporting lists/grids empty layouts)
            val activeNotesToRender = when (uiState.currentFilterMode) {
                NotesFilterMode.ALL -> uiState.notes
                NotesFilterMode.ARCHIVE -> archivedNotes
                NotesFilterMode.TRASH -> trashNotes
            }

            if (activeNotesToRender.isEmpty()) {
                EmptyStateView(mode = uiState.currentFilterMode)
            } else {
                NotesDisplayArea(
                    notes = activeNotesToRender,
                    isGrid = uiState.isGridLayout,
                    filterMode = uiState.currentFilterMode,
                    onNoteClick = { onNavigateToEditor(it.id) },
                    onTogglePin = { viewModel.togglePin(it.id) },
                    onToggleArchive = { viewModel.toggleArchive(it.id) },
                    onDeleteToTrash = { viewModel.deleteNoteToTrash(it.id) },
                    onRestore = { viewModel.restoreFromTrash(it.id) },
                    onPermanentDelete = { viewModel.permanentlyDeleteNote(it) }
                )
            }
        }
    }

    // CREATE FOLDER DIALOG
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("New Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Tag Color:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val colors = listOf("#42A5F5", "#66BB6A", "#FFA726", "#AB47BC", "#EC407A", "#26A69A")
                    LazyRow {
                        items(colors) { col ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(col)))
                                    .clickable { selectedFolderColor = col }
                                    .padding(4.dp)
                            ) {
                                if (selectedFolderColor == col) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName, selectedFolderColor)
                            newFolderName = ""
                            showCreateFolderDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FilterCategoryTabs(
    currentMode: NotesFilterMode,
    onModeSelected: (NotesFilterMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = listOf(
            Triple(NotesFilterMode.ALL, "Notes", Icons.Default.StickyNote2),
            Triple(NotesFilterMode.ARCHIVE, "Archives", Icons.Default.Archive),
            Triple(NotesFilterMode.TRASH, "Trash Bin", Icons.Default.DeleteOutline)
        )

        options.forEach { (mode, label, icon) ->
            val isSelected = currentMode == mode
            FilterChip(
                selected = isSelected,
                onClick = { onModeSelected(mode) },
                label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                leadingIcon = { Icon(icon, null, modifier = Modifier.size(15.dp)) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF005AC1),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = Color(0xFFEEF0F6),
                    labelColor = Color(0xFF44474E),
                    iconColor = Color(0xFF44474E)
                ),
                border = null
            )
        }
    }
}

@Composable
fun FolderSelectorRow(
    folders: List<Folder>,
    selectedFolderId: Int?,
    onFolderSelected: (Int?) -> Unit,
    onCreateFolderClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            AssistChip(
                onClick = onCreateFolderClick,
                label = { Text("New Folder", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                leadingIcon = { Icon(Icons.Default.CreateNewFolder, null, modifier = Modifier.size(15.dp)) },
                shape = RoundedCornerShape(20.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFEEF0F6),
                    labelColor = Color(0xFF44474E),
                    leadingIconContentColor = Color(0xFF44474E)
                ),
                border = null
            )
        }

        item {
            InputChip(
                selected = selectedFolderId == null,
                onClick = { onFolderSelected(null) },
                label = { Text("All Folders", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                shape = RoundedCornerShape(20.dp),
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = Color(0xFF005AC1),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFEEF0F6),
                    labelColor = Color(0xFF44474E)
                ),
                border = null
            )
        }

        items(folders) { folder ->
            val isSelected = selectedFolderId == folder.id
            InputChip(
                selected = isSelected,
                onClick = { onFolderSelected(folder.id) },
                label = { Text(folder.name, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                shape = RoundedCornerShape(20.dp),
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = Color(0xFF005AC1),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFEEF0F6),
                    labelColor = Color(0xFF44474E)
                ),
                border = null,
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(folder.colorHex)))
                    )
                }
            )
        }
    }
}

@Composable
fun ColorTagFilterRow(
    selectedColor: String?,
    onColorSelected: (String?) -> Unit
) {
    val tagColors = listOf("#EC407A", "#FF7043", "#FFEE58", "#66BB6A", "#26C6DA", "#AB47BC")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text(
                "Filter tag:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        item {
            ElevatedAssistChip(
                onClick = { onColorSelected(null) },
                label = { Text("Clear Tag", fontSize = 10.sp) },
                shape = RoundedCornerShape(14.dp)
            )
        }

        items(tagColors) { hex ->
            val isSelected = selectedColor == hex
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(hex)))
                    .clickable { onColorSelected(if (isSelected) null else hex) }
                    .padding(2.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun NotesDisplayArea(
    notes: List<Note>,
    isGrid: Boolean,
    filterMode: NotesFilterMode,
    onNoteClick: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    onToggleArchive: (Note) -> Unit,
    onDeleteToTrash: (Note) -> Unit,
    onRestore: (Note) -> Unit,
    onPermanentDelete: (Note) -> Unit
) {
    val pinnedNotes = if (filterMode == NotesFilterMode.ALL) notes.filter { it.isPinned } else emptyList()
    val otherNotes = if (filterMode == NotesFilterMode.ALL) notes.filter { !it.isPinned } else notes

    if (isGrid) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pinnedNotes.isNotEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PINNED NOTES",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF44474E),
                                letterSpacing = 1.sp
                            ),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "VIEW ALL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF005AC1)
                            )
                        )
                    }
                }

                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                    NoteCompactCard(
                        note = note,
                        filterMode = filterMode,
                        onClick = { onNoteClick(note) },
                        onTogglePin = { onTogglePin(note) },
                        onToggleArchive = { onToggleArchive(note) },
                        onDeleteToTrash = { onDeleteToTrash(note) },
                        onRestore = { onRestore(note) },
                        onPermanentDelete = { onPermanentDelete(note) }
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (pinnedNotes.isNotEmpty()) "RECENT NOTES" else "ALL NOTES",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        letterSpacing = 1.sp
                    ),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(otherNotes, key = { "other_${it.id}" }) { note ->
                NoteCompactCard(
                    note = note,
                    filterMode = filterMode,
                    onClick = { onNoteClick(note) },
                    onTogglePin = { onTogglePin(note) },
                    onToggleArchive = { onToggleArchive(note) },
                    onDeleteToTrash = { onDeleteToTrash(note) },
                    onRestore = { onRestore(note) },
                    onPermanentDelete = { onPermanentDelete(note) }
                )
            }
        }
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PINNED NOTES",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF44474E),
                                letterSpacing = 1.sp
                            ),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "VIEW ALL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF005AC1)
                            )
                        )
                    }
                }

                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                    NoteCompactCard(
                        note = note,
                        filterMode = filterMode,
                        onClick = { onNoteClick(note) },
                        onTogglePin = { onTogglePin(note) },
                        onToggleArchive = { onToggleArchive(note) },
                        onDeleteToTrash = { onDeleteToTrash(note) },
                        onRestore = { onRestore(note) },
                        onPermanentDelete = { onPermanentDelete(note) }
                    )
                }
            }

            item {
                Text(
                    text = if (pinnedNotes.isNotEmpty()) "RECENT NOTES" else "ALL NOTES",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        letterSpacing = 1.sp
                    ),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(otherNotes, key = { "other_${it.id}" }) { note ->
                NoteCompactCard(
                    note = note,
                    filterMode = filterMode,
                    onClick = { onNoteClick(note) },
                    onTogglePin = { onTogglePin(note) },
                    onToggleArchive = { onToggleArchive(note) },
                    onDeleteToTrash = { onDeleteToTrash(note) },
                    onRestore = { onRestore(note) },
                    onPermanentDelete = { onPermanentDelete(note) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCompactCard(
    note: Note,
    filterMode: NotesFilterMode,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onDeleteToTrash: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val baseTagColor = note.colorTag?.let { Color(android.graphics.Color.parseColor(it)) }
        ?: Color(0xFF005AC1) // Default to high density signature blue

    // Determine card background
    val cardBackground = if (note.isPinned) {
        // Pinned notes get beautiful pastel background colors matching the theme
        baseTagColor.copy(alpha = 0.15f)
    } else {
        // Recent notes get sleek white/surface background
        MaterialTheme.colorScheme.surface
    }

    val cardBorderColor = if (note.isPinned) {
        baseTagColor.copy(alpha = 0.4f)
    } else {
        Color(0xFFE1E2EC)
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            cardBorderColor
        ),
        shape = RoundedCornerShape(20.dp), // Styled with 20.dp rounded corners (HTML mockup-like)
        elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Round Folder/Icon container colored with baseTagColor
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(baseTagColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Filled.Description,
                    contentDescription = null,
                    tint = baseTagColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text block (title, content, date modified)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (note.title.isBlank()) "Untitled Note" else note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = note.content,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Updated ${df.format(Date(note.dateModified))}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    if (note.colorTag != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(baseTagColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TAGGED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = baseTagColor
                            )
                        }
                    }
                }
            }

            // Trailing options button
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                when (filterMode) {
                    NotesFilterMode.ALL -> {
                        DropdownMenuItem(
                            text = { Text(if (note.isPinned) "Unpin Note" else "Pin Note") },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) },
                            onClick = {
                                onTogglePin()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to Archive") },
                            leadingIcon = { Icon(Icons.Default.Archive, null) },
                            onClick = {
                                onToggleArchive()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to Trash") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = {
                                onDeleteToTrash()
                                showMenu = false
                            }
                        )
                    }
                    NotesFilterMode.ARCHIVE -> {
                        DropdownMenuItem(
                            text = { Text("Unarchive Note") },
                            leadingIcon = { Icon(Icons.Default.Unarchive, null) },
                            onClick = {
                                onToggleArchive()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to Trash") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = {
                                onDeleteToTrash()
                                showMenu = false
                            }
                        )
                    }
                    NotesFilterMode.TRASH -> {
                        DropdownMenuItem(
                            text = { Text("Restore Note") },
                            leadingIcon = { Icon(Icons.Default.RestoreFromTrash, null) },
                            onClick = {
                                onRestore()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Permanently") },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, null) },
                            onClick = {
                                onPermanentDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(mode: NotesFilterMode) {
    val (title, sub, icon) = when (mode) {
        NotesFilterMode.ALL -> Triple("Vault is empty", "Start taking down notes cleanly in Inter Note.", Icons.Outlined.Assignment)
        NotesFilterMode.ARCHIVE -> Triple("No archived notes", "Keep important but inactive work tucked away securely here.", Icons.Outlined.Archive)
        NotesFilterMode.TRASH -> Triple("Recycle bin is clean", "Trash items are auto-purged securely after 30 days of retention.", Icons.Outlined.DeleteOutline)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sub,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
