package com.example.islamiccorpusvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.ui.components.MoveCategory
import com.example.islamiccorpusvault.ui.components.MoveScholar
import com.example.islamiccorpusvault.ui.components.NoteActionSheet
import com.example.islamiccorpusvault.ui.model.AppNote
import com.example.islamiccorpusvault.ui.model.NoteStore
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenGeneralNotes: () -> Unit = {},
    onOpenScholars: () -> Unit = {},
    onOpenNoteDetail: (title: String, body: String, citation: String) -> Unit = { _, _, _ -> }
) {
    var query by remember { mutableStateOf("") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteBody by remember { mutableStateOf("") }
    var newNoteCitation by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val recentNotes = NoteStore.notes

    val selectedNote = selectedNoteId?.let { id -> recentNotes.firstOrNull { it.id == id } }

    val filtered = if (query.isBlank()) {
        recentNotes
    } else {
        recentNotes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.preview.contains(query, ignoreCase = true) ||
                it.citation.contains(query, ignoreCase = true) ||
                it.container.contains(query, ignoreCase = true)
        }
    }

    val recentVisibleCount = if (filtered.isEmpty()) 1 else minOf(3, filtered.size)
    val pinnedHeaderIndex = 5 + recentVisibleCount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Capture quickly, review clearly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { SearchBar(query = query, onQueryChange = { query = it }) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { }, label = { Text("${recentNotes.size} notes") })
                AssistChip(onClick = { }, label = { Text("${recentNotes.count { it.isPinned }} pinned") })
                AssistChip(onClick = { }, label = { Text("4 scholars") })
            }
        }

        item {
            QuickActions(
                onCreateNote = { showCreateSheet = true },
                onCreateScholar = onOpenScholars,
                onPinned = { scope.launch { listState.animateScrollToItem(index = pinnedHeaderIndex) } }
            )
        }

        item {
            SectionHeader(title = "Recent Notes", actionText = "View all", onAction = { })
        }

        if (filtered.isEmpty()) {
            item {
                EmptyCard(
                    title = "No matching notes",
                    subtitle = "Try another keyword or clear search."
                )
            }
        } else {
            items(items = filtered.take(3), key = { "recent_${it.id}" }) { note ->
                HomeNoteCard(
                    note = note,
                    onContainerClick = { if (note.container == "General Notes") onOpenGeneralNotes() },
                    onClick = { onOpenNoteDetail(note.title, note.preview, note.citation) },
                    onLongPress = { selectedNoteId = note.id }
                )
            }
        }

        item {
            SectionHeader(title = "Pinned", actionText = "Manage", onAction = { })
        }

        val pinned = filtered.filter { it.isPinned }
        if (pinned.isEmpty()) {
            item {
                EmptyCard(
                    title = "Nothing pinned yet",
                    subtitle = "Pin important notes to keep them at the top."
                )
            }
        } else {
            items(items = pinned, key = { "pinned_${it.id}" }) { note ->
                HomeNoteCard(
                    note = note,
                    onContainerClick = { if (note.container == "General Notes") onOpenGeneralNotes() },
                    onClick = { onOpenNoteDetail(note.title, note.preview, note.citation) },
                    onLongPress = { selectedNoteId = note.id }
                )
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }

    if (showCreateSheet) {
        ModalBottomSheet(onDismissRequest = { showCreateSheet = false }) {
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
                    TextButton(onClick = { showCreateSheet = false }) { Text("Cancel") }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val title = newNoteTitle.trim()
                            val body = newNoteBody.trim()
                            if (title.isNotEmpty() || body.isNotEmpty()) {
                                recentNotes.add(
                                    0,
                                    AppNote(
                                        id = System.currentTimeMillis().toString(),
                                        title = if (title.isNotEmpty()) title else "Untitled",
                                        preview = body,
                                        citation = newNoteCitation.trim(),
                                        isPinned = false,
                                        container = "General Notes"
                                    )
                                )
                                newNoteTitle = ""
                                newNoteBody = ""
                                newNoteCitation = ""
                                showCreateSheet = false
                            }
                        }
                    ) { Text("Create") }
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
                    name = "Ibn Taymiyyah",
                    categories = listOf(
                        MoveCategory("Aqeedah", listOf("Asma wa Sifat", "Tawheed")),
                        MoveCategory("Fiqh", listOf("Taharah", "Salah"))
                    )
                ),
                MoveScholar(
                    name = "Ibn al-Qayyim",
                    categories = listOf(
                        MoveCategory("Books", listOf("Madarij", "Zad al-Maad")),
                        MoveCategory("Quotes", listOf("Heart", "Patience"))
                    )
                )
            ),
            onDismiss = { selectedNoteId = null },
            onTogglePin = {
                val index = recentNotes.indexOfFirst { it.id == selectedNote.id }
                if (index >= 0) {
                    recentNotes[index] = recentNotes[index].copy(isPinned = !recentNotes[index].isPinned)
                }
                selectedNoteId = null
            },
            onMoveToGeneral = {
                val index = recentNotes.indexOfFirst { it.id == selectedNote.id }
                if (index >= 0) {
                    recentNotes[index] = recentNotes[index].copy(container = "General Notes")
                }
                selectedNoteId = null
            },
            onMoveToDestination = { destination ->
                val index = recentNotes.indexOfFirst { it.id == selectedNote.id }
                if (index >= 0) {
                    recentNotes[index] = recentNotes[index].copy(container = destination)
                }
                selectedNoteId = null
            },
            onDelete = {
                recentNotes.removeAll { it.id == selectedNote.id }
                selectedNoteId = null
            }
        )
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text("Search notes, citations, scholars") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 6.dp)
        )
    }
}

@Composable
private fun QuickActions(
    onCreateNote: () -> Unit,
    onCreateScholar: () -> Unit,
    onPinned: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionTile(
            label = "New Note",
            icon = Icons.Outlined.NoteAdd,
            onClick = onCreateNote,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            label = "Scholars",
            icon = Icons.Outlined.PersonAdd,
            onClick = onCreateScholar,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            label = "Pinned",
            icon = Icons.Outlined.PushPin,
            onClick = onPinned,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier
            .aspectRatio(1.35f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String, onAction: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) { Text(actionText) }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HomeNoteCard(
    note: AppNote,
    onContainerClick: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = note.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = note.citation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = onContainerClick,
                label = { Text(note.container) }
            )
        }
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
