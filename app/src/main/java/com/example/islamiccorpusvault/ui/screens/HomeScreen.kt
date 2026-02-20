package com.example.islamiccorpusvault.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.components.MoveCategory
import com.example.islamiccorpusvault.ui.components.MoveScholar
import com.example.islamiccorpusvault.ui.components.NoteActionSheet
import com.example.islamiccorpusvault.ui.model.AppNote
import com.example.islamiccorpusvault.ui.util.toPlainText
import kotlinx.coroutines.launch

data class HomeNoteItem(
    val note: AppNote,
    val tagNames: List<String>
)

@Composable
fun HomeScreen(
    onOpenGeneralNotes: () -> Unit = {},
    onOpenScholars: () -> Unit = {},
    onOpenNoteDetail: (noteId: String) -> Unit = {},
    onOpenNoteEditor: (noteId: String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val notesRepository = AppContainer.notesRepository

    val notes by notesRepository.observeAll().collectAsState(initial = emptyList())
    val noteTags by notesRepository.observeAllNoteTagNames().collectAsState(initial = emptyList())

    val tagsByNoteId = remember(noteTags) {
        noteTags.groupBy(keySelector = { it.noteId }, valueTransform = { it.tagName })
    }

    val selectedNote = selectedNoteId?.let { id -> notes.firstOrNull { it.id == id } }

    val filtered = remember(notes, query, tagsByNoteId) {
        val items = notes.map { note ->
            HomeNoteItem(
                note = note,
                tagNames = tagsByNoteId[note.id].orEmpty()
            )
        }

        if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.note.title.contains(query, ignoreCase = true) ||
                    toPlainText(item.note.preview).contains(query, ignoreCase = true) ||
                    item.note.citation.contains(query, ignoreCase = true) ||
                    item.note.container.contains(query, ignoreCase = true) ||
                    item.tagNames.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }

    val recentActivity = filtered.take(5)
    val pinnedHeaderIndex = if (recentActivity.isEmpty()) 5 else 4 + recentActivity.size

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SearchBar(query = query, onQueryChange = { query = it })
        }

        item {
            StatsRow(
                notesCount = notes.size,
                pinnedCount = notes.count { it.isPinned },
                scholarsCount = 4
            )
        }

        item {
            QuickActionsRow(
                onCreateNote = {
                    val noteId = System.currentTimeMillis().toString()
                    scope.launch {
                        notesRepository.upsert(
                            AppNote(
                                id = noteId,
                                title = "Untitled",
                                preview = "",
                                citation = "",
                                isPinned = false,
                                container = "General Notes"
                            )
                        )
                    }
                    onOpenNoteEditor(noteId)
                },
                onCreateScholar = onOpenScholars,
                onPinned = { scope.launch { listState.animateScrollToItem(index = pinnedHeaderIndex) } }
            )
        }

        item {
            SectionHeader(
                title = "Recent activity",
                actionText = "View all",
                onActionClick = onOpenGeneralNotes
            )
        }

        if (recentActivity.isEmpty()) {
            item {
                EmptyCard(
                    title = "No activity yet",
                    subtitle = "Create or edit a note to populate this list."
                )
            }
        } else {
            items(items = recentActivity, key = { "activity_${it.note.id}" }) { item ->
                RecentActivityCard(
                    note = item.note,
                    tagNames = item.tagNames,
                    onClick = { onOpenNoteDetail(item.note.id) },
                    onLongPress = { selectedNoteId = item.note.id }
                )
            }
        }

        item {
            Spacer(Modifier.height(2.dp))
            SectionHeader(title = "Pinned")
        }

        val pinned = filtered.filter { it.note.isPinned }
        if (pinned.isEmpty()) {
            item {
                EmptyCard(
                    title = "Nothing pinned yet",
                    subtitle = "Long-press any item to pin it."
                )
            }
        } else {
            items(items = pinned, key = { "pinned_${it.note.id}" }) { item ->
                RecentActivityCard(
                    note = item.note,
                    tagNames = item.tagNames,
                    onClick = { onOpenNoteDetail(item.note.id) },
                    onLongPress = { selectedNoteId = item.note.id }
                )
            }
        }

        item { Spacer(Modifier.height(10.dp)) }
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
                scope.launch { notesRepository.togglePin(selectedNote.id) }
                selectedNoteId = null
            },
            onMoveToGeneral = {
                scope.launch { notesRepository.move(selectedNote.id, "General Notes") }
                selectedNoteId = null
                onOpenGeneralNotes()
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
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.96f),
            modifier = Modifier.weight(1f)
        )
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(text = actionText)
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text("Search notes, tags, scholars") },
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
                .height(50.dp)
                .padding(horizontal = 6.dp)
        )
    }
}

@Composable
private fun StatsRow(
    notesCount: Int,
    pinnedCount: Int,
    scholarsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = {}, label = { Text("$notesCount notes") })
        AssistChip(onClick = {}, label = { Text("$pinnedCount pinned") })
        AssistChip(onClick = {}, label = { Text("$scholarsCount scholars") })
    }
}

@Composable
private fun QuickActionsRow(
    onCreateNote: () -> Unit,
    onCreateScholar: () -> Unit,
    onPinned: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionCard(
            label = "New",
            icon = Icons.AutoMirrored.Outlined.NoteAdd,
            onClick = onCreateNote,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = "Scholars",
            icon = Icons.Outlined.PersonAdd,
            onClick = onCreateScholar,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = "Pinned",
            icon = Icons.Outlined.PushPin,
            onClick = onPinned,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (pressed) 1.dp else 2.dp,
        shadowElevation = if (pressed) 1.dp else 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(Modifier.width(7.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentActivityCard(
    note: AppNote,
    tagNames: List<String>,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val title = note.title.ifBlank { "Untitled" }
    val preview = activityPreview(note)

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (pressed) 1.dp else 2.dp,
        shadowElevation = if (pressed) 1.dp else 9.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }

                Text(
                    text = formatEditedTime(note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                    maxLines = 1,
                    modifier = Modifier.alpha(0.9f)
                )
            }

            if (tagNames.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                TagPills(tagNames = tagNames)
            }

            Spacer(Modifier.height(7.dp))

            Surface(
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TagPills(tagNames: List<String>) {
    val visible = tagNames.take(3)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        visible.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        val remaining = tagNames.size - visible.size
        if (remaining > 0) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "+$remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    } 
}

private fun activityPreview(note: AppNote): String {
    val preview = toPlainText(note.preview)
        .replace("\\s+".toRegex(), " ")
        .trim()
    return if (preview.isBlank()) "No preview yet" else preview.take(160)
}

private fun formatEditedTime(note: AppNote): String {
    if (note.updatedAt <= 0L) return "recent"
    val delta = System.currentTimeMillis() - note.updatedAt
    if (delta < 0L) return "recent"
    if (delta < 60_000L) return "just now"
    if (delta < 3_600_000L) return "${delta / 60_000L}m"
    if (delta < 86_400_000L) return "${delta / 3_600_000L}h"
    return "${delta / 86_400_000L}d"
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
