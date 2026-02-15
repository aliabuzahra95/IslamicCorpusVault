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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.ui.components.MoveCategory
import com.example.islamiccorpusvault.ui.components.MoveScholar
import com.example.islamiccorpusvault.ui.components.NoteActionSheet

private data class SubcategoryNote(
    val id: String,
    val title: String,
    val body: String,
    val citation: String,
    val isPinned: Boolean = false,
    val container: String = "General Notes"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryScreen(
    scholarName: String,
    categoryName: String,
    subcategoryName: String,
    onNoteClick: (noteId: String, title: String, body: String, citation: String) -> Unit
) {
    val notes = remember { mutableStateListOf<SubcategoryNote>() }
    var showSheet by remember { mutableStateOf(false) }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    val selectedNote = selectedNoteId?.let { id -> notes.firstOrNull { it.id == id } }

    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteBody by remember { mutableStateOf("") }
    var newNoteCitation by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
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
                            body = note.body,
                            citation = note.citation,
                            isPinned = note.isPinned,
                            container = note.container,
                            onClick = { onNoteClick(note.id, note.title, note.body, note.citation) },
                            onLongPress = { selectedNoteId = note.id }
                        )
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("New note", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = newNoteTitle,
                    onValueChange = { newNoteTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Title") }
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = newNoteBody,
                    onValueChange = { newNoteBody = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Body text") }
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = newNoteCitation,
                    onValueChange = { newNoteCitation = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("Citation (optional)") }
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showSheet = false }) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(6.dp))

                    Button(
                        onClick = {
                            val title = newNoteTitle.trim()
                            val body = newNoteBody.trim()
                            if (title.isNotEmpty() || body.isNotEmpty()) {
                                notes.add(
                                    SubcategoryNote(
                                        id = System.currentTimeMillis().toString(),
                                        title = if (title.isNotEmpty()) title else "Untitled",
                                        body = body,
                                        citation = newNoteCitation.trim(),
                                        isPinned = false,
                                        container = "General Notes"
                                    )
                                )
                                newNoteTitle = ""
                                newNoteBody = ""
                                newNoteCitation = ""
                                showSheet = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }

                Spacer(Modifier.height(10.dp))
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
                val index = notes.indexOfFirst { it.id == selectedNote.id }
                if (index >= 0) notes[index] = notes[index].copy(isPinned = !notes[index].isPinned)
                selectedNoteId = null
            },
            onMoveToGeneral = {
                val index = notes.indexOfFirst { it.id == selectedNote.id }
                if (index >= 0) notes[index] = notes[index].copy(container = "General Notes")
                selectedNoteId = null
            },
            onMoveToDestination = { destination ->
                val index = notes.indexOfFirst { it.id == selectedNote.id }
                if (index >= 0) notes[index] = notes[index].copy(container = destination)
                selectedNoteId = null
            },
            onDelete = {
                notes.removeAll { it.id == selectedNote.id }
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
