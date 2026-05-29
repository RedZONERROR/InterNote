package com.inter.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.inter.data.local.entities.Folder
import com.inter.data.local.entities.Note
import com.inter.ui.main.NoteViewModel
import com.inter.ui.theme.safeParseColor
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeOut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: NoteViewModel,
    noteId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val foldersList = uiState.folders

    // Form inputs state
    var title by remember { mutableStateOf("") }
    var contentValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFolderId by remember { mutableStateOf<Int?>(null) }
    var selectedColorTag by remember { mutableStateOf<String?>(null) }

    // Dropdown selectors controls
    var showFolderDropdown by remember { mutableStateOf(false) }
    var showColorPalette by remember { mutableStateOf(false) }

    var initialNoteLoaded by remember { mutableStateOf(false) }
    var originalNote: Note? by remember { mutableStateOf(null) }

    // 1. Load initial note details if editing
    LaunchedEffect(noteId) {
        if (noteId != -1) {
            val note = viewModel.getNoteById(noteId)
            if (note != null) {
                originalNote = note
                title = note.title
                contentValue = TextFieldValue(note.content, selection = TextRange(note.content.length))
                selectedFolderId = note.folderId
                selectedColorTag = note.colorTag
            }
        }
        initialNoteLoaded = true
    }

    // 2. Debounced Auto-Save Engine (Triggered 600ms after text inputs rest)
    LaunchedEffect(title, contentValue.text, selectedFolderId, selectedColorTag) {
        if (!initialNoteLoaded) return@LaunchedEffect
        delay(600) // Debounce delay
        
        // Save execution block
        if (noteId == -1) {
            if (title.isNotBlank() || contentValue.text.isNotBlank()) {
                viewModel.createNote(title, contentValue.text, selectedFolderId, selectedColorTag)
            }
        } else {
            val noteToSave = originalNote?.copy(
                title = title,
                content = contentValue.text,
                folderId = selectedFolderId,
                colorTag = selectedColorTag,
                dateModified = System.currentTimeMillis()
            )
            if (noteToSave != null) {
                viewModel.updateNote(noteToSave)
            }
        }
    }

    // 3. Lifecycle-aware Auto-Save (Saves instantly on pause or screen collapse)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (initialNoteLoaded) {
                    if (noteId == -1) {
                        if (title.isNotBlank() || contentValue.text.isNotBlank()) {
                            viewModel.createNote(title, contentValue.text, selectedFolderId, selectedColorTag)
                        }
                    } else {
                        originalNote?.let {
                            val saved = it.copy(
                                title = title,
                                content = contentValue.text,
                                folderId = selectedFolderId,
                                colorTag = selectedColorTag,
                                dateModified = System.currentTimeMillis()
                            )
                            viewModel.updateNote(saved)
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Helper to insert Markdown syntax around current cursor or selection
    fun insertMarkup(syntaxStart: String, syntaxEnd: String) {
        val currentText = contentValue.text
        val selection = contentValue.selection
        val selectedText = currentText.substring(selection.start, selection.end)
        val prefix = currentText.substring(0, selection.start)
        val suffix = currentText.substring(selection.end)
        
        val newText = "$prefix$syntaxStart$selectedText$syntaxEnd$suffix"
        val newCursorPos = selection.start + syntaxStart.length + selectedText.length + syntaxEnd.length
        
        contentValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPos)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "New Note" else "Edit Note", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("save_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back and Auto-Save")
                    }
                },
                actions = {
                    // Folder allocation selector dropdown
                    IconButton(onClick = { showFolderDropdown = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = "Select Folder",
                            tint = if (selectedFolderId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Theme Tag Color palette dropdown selector
                    IconButton(onClick = { showColorPalette = !showColorPalette }) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Select Tag Color",
                            tint = if (selectedColorTag != null) safeParseColor(selectedColorTag) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "Done and Auto-Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Folders selection dropdown overlay
            DropdownMenu(
                expanded = showFolderDropdown,
                onDismissRequest = { showFolderDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("No Folder (Unassigned)") },
                    onClick = {
                        selectedFolderId = null
                        showFolderDropdown = false
                    },
                    leadingIcon = { Icon(Icons.Default.Folder, null, tint = Color.LightGray) }
                )
                foldersList.forEach { folder: Folder ->
                    DropdownMenuItem(
                        text = { Text(folder.name) },
                        onClick = {
                            selectedFolderId = folder.id
                            showFolderDropdown = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Folder,
                                null,
                                tint = safeParseColor(folder.colorHex)
                            )
                        }
                    )
                }
            }

            // Slide out color tag palette selector strip
            AnimatedVisibility(
                visible = showColorPalette,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                val paletteColors = listOf("#EC407A", "#FF7043", "#FFEE58", "#66BB6A", "#26C6DA", "#AB47BC")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("Select tag color label:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { selectedColorTag = null; showColorPalette = false }
                                .padding(2.dp)
                        ) {
                            Icon(Icons.Default.Clear, "None", tint = Color.Red, modifier = Modifier.align(Alignment.Center).size(16.dp))
                        }

                        paletteColors.forEach { colorStr ->
                            val colorVal = safeParseColor(colorStr)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colorVal)
                                    .clickable { selectedColorTag = colorStr; showColorPalette = false }
                                    .padding(4.dp)
                            ) {
                                if (selectedColorTag == colorStr) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Note TITLE Input Field
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("note_title_input"),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Dynamic Markup Formatting rich toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEEF0F6))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { insertMarkup("**", "**") }) {
                    Icon(imageVector = Icons.Filled.FormatBold, contentDescription = "Format Bold")
                }
                IconButton(onClick = { insertMarkup("*", "*") }) {
                    Icon(imageVector = Icons.Filled.FormatItalic, contentDescription = "Format Italic")
                }
                IconButton(onClick = { insertMarkup("<u>", "</u>") }) {
                    Icon(imageVector = Icons.Filled.FormatUnderlined, contentDescription = "Format Underline")
                }
                IconButton(onClick = { insertMarkup("~~", "~~") }) {
                    Icon(imageVector = Icons.Filled.FormatStrikethrough, contentDescription = "Format Strikethrough")
                }
                IconButton(onClick = { insertMarkup("- ", "") }) {
                    Icon(imageVector = Icons.Filled.FormatListBulleted, contentDescription = "Format Bullet List")
                }
                IconButton(onClick = { insertMarkup("# ", "") }) {
                    Icon(imageVector = Icons.Default.Title, contentDescription = "Format Header")
                }
            }

            // Note CONTENT main multi-line textbox utilizing Live Markdown Formatting transformation
            TextField(
                value = contentValue,
                onValueChange = { contentValue = it },
                placeholder = { Text("Write your notes here... Type formatting syntaxes or use the toolbar above.", fontSize = 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .testTag("note_content_input"),
                textStyle = MaterialTheme.typography.bodyLarge,
                visualTransformation = MarkdownVisualTransformation(isSystemInDarkTheme()),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}
