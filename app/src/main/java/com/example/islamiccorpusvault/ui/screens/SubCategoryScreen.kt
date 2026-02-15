package com.example.islamiccorpusvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.components.MoveCategory
import com.example.islamiccorpusvault.ui.components.MoveScholar
import com.example.islamiccorpusvault.ui.components.NoteActionSheet
import com.example.islamiccorpusvault.ui.model.AppNote
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryScreen(
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
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    val selectedNote = selectedNoteId?.let { id -> notes.firstOrNull { it.id == id } }

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

            if (notes.isEmpty()) {
                Text(
                    text = "No notes yet. Tap + to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = notes, key = { it.id }) { note ->
                        SubcategoryNoteCard(
                            title = note.title,
                            body = note.preview,
                            citation = note.citation,
                            isPinned = note.isPinned,
                            container = note.container,
                            onClick = { onNoteClick(note.id) },
                            onLongPress = { selectedNoteId = note.id }
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SubcategoryNoteCard(
    title: String,
    body: String,
    citation: String,
    isPinned: Boolean,
    container: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                Text(text = title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (body.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (citation.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = citation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = container,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
