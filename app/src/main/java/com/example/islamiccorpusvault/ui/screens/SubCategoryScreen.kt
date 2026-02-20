package com.example.islamiccorpusvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.components.CompactNoteCard
import com.example.islamiccorpusvault.ui.components.MoveCategory
import com.example.islamiccorpusvault.ui.components.MoveScholar
import com.example.islamiccorpusvault.ui.components.NoteActionSheet
import com.example.islamiccorpusvault.ui.components.normalizedPreviewText
import com.example.islamiccorpusvault.ui.model.AppNote
import kotlinx.coroutines.launch

private data class SubcategoryNoteItem(
    val note: AppNote,
    val tagNames: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryScreen(
    scholarId: String,
    scholarName: String,
    categoryName: String,
    subcategoryName: String,
    onNoteClick: (noteId: String) -> Unit,
    onCreateNote: (noteId: String) -> Unit
) {
    val notesRepository = AppContainer.notesRepository
    val scope = rememberCoroutineScope()
    val containerPath = "$scholarName > $categoryName > $subcategoryName"
    val notes by notesRepository.observeByContainer(containerPath).collectAsState(initial = emptyList())
    val noteTags by notesRepository.observeAllNoteTagNames().collectAsState(initial = emptyList())
    val tagsByNoteId = remember(noteTags) {
        noteTags.groupBy(keySelector = { it.noteId }, valueTransform = { it.tagName })
    }

    var query by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    val selectedNote = selectedNoteId?.let { id -> notes.firstOrNull { it.id == id } }

    val filtered = remember(notes, query, tagsByNoteId) {
        val items = notes.map { note ->
            SubcategoryNoteItem(note = note, tagNames = tagsByNoteId[note.id].orEmpty())
        }
        if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.note.title.contains(query, ignoreCase = true) ||
                    normalizedPreviewText(item.note.preview).contains(query, ignoreCase = true) ||
                    item.note.container.contains(query, ignoreCase = true) ||
                    item.tagNames.any { it.contains(query, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
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
                                container = containerPath
                            )
                        )
                    }
                    onCreateNote(noteId)
                }
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add note")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = scholarName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subcategoryName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            SearchBar(query = query, onQueryChange = { query = it })
            Spacer(Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                Text(
                    text = if (query.isBlank()) "No notes yet. Tap + to create one." else "No matching notes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = filtered, key = { it.note.id }) { item ->
                        CompactNoteCard(
                            note = item.note,
                            tagNames = item.tagNames,
                            onClick = { onNoteClick(item.note.id) },
                            onLongPress = { selectedNoteId = item.note.id }
                        )
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
                            subcategories = listOf(subcategoryName)
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
