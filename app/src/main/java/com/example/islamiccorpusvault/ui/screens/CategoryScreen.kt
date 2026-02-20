package com.example.islamiccorpusvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.components.CompactNoteCard
import com.example.islamiccorpusvault.ui.components.MoveCategory
import com.example.islamiccorpusvault.ui.components.MoveScholar
import com.example.islamiccorpusvault.ui.components.NoteActionSheet
import com.example.islamiccorpusvault.ui.components.normalizedPreviewText
import com.example.islamiccorpusvault.data.repo.ScholarSubcategory
import com.example.islamiccorpusvault.ui.model.AppNote
import kotlinx.coroutines.launch

private enum class SheetMode { ACTIONS, NEW_SUBCATEGORY }

private data class CategoryNoteItem(
    val note: AppNote,
    val tagNames: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    scholarId: String,
    scholarName: String,
    categoryName: String,
    onCreateSubcategory: () -> Unit,
    onCreateNote: (noteId: String) -> Unit,
    onSubcategoryClick: (subcategoryId: String, subcategoryName: String) -> Unit,
    onNoteClick: (noteId: String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var sheetMode by remember { mutableStateOf(SheetMode.ACTIONS) }
    val notesRepository = AppContainer.notesRepository
    val corpusRepository = AppContainer.corpusRepository
    val scope = rememberCoroutineScope()

    val notes by notesRepository.observeByContainer(categoryName).collectAsState(initial = emptyList())
    val noteTags by notesRepository.observeAllNoteTagNames().collectAsState(initial = emptyList())
    val tagsByNoteId = remember(noteTags) {
        noteTags.groupBy(keySelector = { it.noteId }, valueTransform = { it.tagName })
    }
    val subcategories by corpusRepository.observeSubcategories(scholarId, categoryName).collectAsState(initial = emptyList())

    // form state
    var query by remember { mutableStateOf("") }
    var newSubcategoryName by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    val selectedNote = selectedNoteId?.let { id -> notes.firstOrNull { it.id == id } }
    val filteredNotes = remember(notes, query, tagsByNoteId) {
        val items = notes.map { note ->
            CategoryNoteItem(note = note, tagNames = tagsByNoteId[note.id].orEmpty())
        }
        if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.note.title.contains(query, ignoreCase = true) ||
                    normalizedPreviewText(item.note.preview).contains(query, ignoreCase = true) ||
                    item.note.citation.contains(query, ignoreCase = true) ||
                    item.note.container.contains(query, ignoreCase = true) ||
                    item.tagNames.any { it.contains(query, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { sheetMode = SheetMode.ACTIONS; showSheet = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = scholarName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // --- Subcategories section ---
                item {
                    Text(
                        text = "Subcategories",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    if (subcategories.isEmpty()) {
                        Text(
                            text = "No subcategories yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items = subcategories, key = { it.id }) { sc ->
                                SubcategoryPill(
                                    name = sc.name,
                                    onClick = { onSubcategoryClick(sc.id, sc.name) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                // --- Notes section ---
                item {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    SearchBar(query = query, onQueryChange = { query = it })
                    Spacer(Modifier.height(10.dp))
                }

                if (filteredNotes.isEmpty()) {
                    item {
                        Text(
                            text = if (query.isBlank()) "No notes yet. Tap + and create one." else "No matching notes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(items = filteredNotes, key = { it.note.id }) { item ->
                        CompactNoteCard(
                            note = item.note,
                            tagNames = item.tagNames,
                            onClick = { onNoteClick(item.note.id) },
                            onLongPress = { selectedNoteId = item.note.id }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            when (sheetMode) {
                SheetMode.ACTIONS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                            ) {
                                Spacer(Modifier.size(width = 34.dp, height = 4.dp))
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Create",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "In $categoryName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))

                        TelegramSheetRow(
                            title = "Subcategory",
                            subtitle = "Group notes",
                            icon = Icons.Outlined.Folder,
                            onClick = {
                                sheetMode = SheetMode.NEW_SUBCATEGORY
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        TelegramSheetRow(
                            title = "Note",
                            subtitle = "Open dedicated editor",
                            icon = Icons.AutoMirrored.Outlined.NoteAdd,
                            onClick = {
                                val noteId = System.currentTimeMillis().toString()
                                scope.launch {
                                    notesRepository.upsert(
                                        AppNote(
                                            id = noteId,
                                            title = "Untitled",
                                            preview = "",
                                            citation = "",
                                            isPinned = false,
                                            container = categoryName
                                        )
                                    )
                                }
                                showSheet = false
                                onCreateNote(noteId)
                            }
                        )

                        Spacer(Modifier.height(14.dp))
                    }
                }

                SheetMode.NEW_SUBCATEGORY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("New subcategory", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newSubcategoryName,
                            onValueChange = { newSubcategoryName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Name (e.g., Al-Uluw)") },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { sheetMode = SheetMode.ACTIONS }
                            ) { Text("Back") }

                            Spacer(Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    val name = newSubcategoryName.trim()
                                    if (name.isNotEmpty()) {
                                        val id = "${scholarId}_${categoryName.lowercase().replace(" ", "_")}_${name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}"
                                        scope.launch {
                                            corpusRepository.upsertSubcategory(
                                                ScholarSubcategory(
                                                    id = id,
                                                    scholarId = scholarId,
                                                    categoryName = categoryName,
                                                    name = name
                                                )
                                            )
                                        }
                                        newSubcategoryName = ""
                                        showSheet = false
                                        sheetMode = SheetMode.ACTIONS
                                        onCreateSubcategory()
                                    }
                                }
                            ) { Text("Create") }
                        }

                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (selectedNote != null) {
        NoteActionSheet(
            title = selectedNote.title,
            isPinned = selectedNote.isPinned,
            moveTree = listOf(
                MoveScholar(
                    name = scholarName,
                    categories = listOf(
                        MoveCategory(
                            name = categoryName,
                            subcategories = subcategories.map { it.name }.ifEmpty { listOf("General") }
                        )
                    )
                )
            ),
            onDismiss = { selectedNoteId = null },
            onTogglePin = {
                scope.launch { notesRepository.togglePin(selectedNote.id) }
                selectedNoteId = null
            },
            onMoveToGeneral = {
                scope.launch { notesRepository.move(selectedNote.id, "General Notes") }
                selectedNoteId = null
            },
            onMoveToDestination = { destination ->
                scope.launch { notesRepository.move(selectedNote.id, destination) }
                selectedNoteId = null
            },
            onDelete = {
                scope.launch { notesRepository.deleteById(selectedNote.id) }
                selectedNoteId = null
            }
        )
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text("Search notes and tags") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun TelegramSheetRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(9.dp).size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun SubcategoryPill(
    name: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
