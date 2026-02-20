package com.example.islamiccorpusvault.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.components.DisplayModeSheet
import com.example.islamiccorpusvault.ui.components.AppSearchField
import com.example.islamiccorpusvault.ui.components.MoveCategory
import com.example.islamiccorpusvault.ui.components.MoveScholar
import com.example.islamiccorpusvault.ui.components.NoteActionSheet
import com.example.islamiccorpusvault.ui.components.NoteDisplayCard
import com.example.islamiccorpusvault.ui.components.NoteUiItem
import com.example.islamiccorpusvault.ui.model.AppNote
import com.example.islamiccorpusvault.ui.settings.NoteDisplayMode
import com.example.islamiccorpusvault.ui.util.toPlainText
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenGeneralNotes: () -> Unit = {},
    onOpenNoteDetail: (noteId: String) -> Unit = {},
    onOpenNoteEditor: (noteId: String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    var showDisplaySheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val notesRepository = AppContainer.notesRepository
    val uiPrefs = AppContainer.uiPrefsRepository

    val displayMode by uiPrefs.displayModeFlow.collectAsState(initial = NoteDisplayMode.LIST)
    val notes by notesRepository.observeAll().collectAsState(initial = emptyList())
    val noteTags by notesRepository.observeAllNoteTagNames().collectAsState(initial = emptyList())
    val attachments by notesRepository.observeAllAttachments().collectAsState(initial = emptyList())

    val tagsByNoteId = remember(noteTags) {
        noteTags.groupBy(keySelector = { it.noteId }, valueTransform = { it.tagName })
    }
    val attachmentCountByNoteId = remember(attachments) {
        attachments.groupingBy { it.noteId }.eachCount()
    }

    val selectedNote = selectedNoteId?.let { id -> notes.firstOrNull { it.id == id } }

    val filtered = remember(notes, query, tagsByNoteId, attachmentCountByNoteId) {
        val items = notes.map { note ->
            NoteUiItem(
                note = note,
                tagNames = tagsByNoteId[note.id].orEmpty(),
                attachmentCount = attachmentCountByNoteId[note.id] ?: 0
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
    val pinned = filtered.filter { it.note.isPinned }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SearchBar(query = query, onQueryChange = { query = it })
        }

        item {
            StatsRow(
                notesCount = notes.size,
                pinnedCount = notes.count { it.isPinned }
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
                onPinned = { scope.launch { listState.animateScrollToItem(index = pinnedHeaderIndex) } }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.96f),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDisplaySheet = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Display mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onOpenGeneralNotes) {
                    Text("View all")
                }
            }
        }

        if (recentActivity.isEmpty()) {
            item {
                EmptyCard(
                    title = "No activity yet",
                    subtitle = "Create or edit a note to populate this list."
                )
            }
        } else {
            when (displayMode) {
                NoteDisplayMode.LIST -> {
                    items(items = recentActivity, key = { "activity_${it.note.id}" }) { item ->
                        NoteDisplayCard(
                            item = item,
                            mode = displayMode,
                            onClick = { onOpenNoteDetail(item.note.id) },
                            onLongPress = { selectedNoteId = item.note.id }
                        )
                    }
                }

                NoteDisplayMode.ICONS,
                NoteDisplayMode.GRID,
                NoteDisplayMode.BOOK -> {
                    item {
                        NotesGridBlock(
                            notes = recentActivity,
                            mode = displayMode,
                            onOpenNoteDetail = onOpenNoteDetail,
                            onLongPress = { selectedNoteId = it }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(2.dp))
            SectionHeader(title = "Pinned")
        }

        if (pinned.isEmpty()) {
            item {
                EmptyCard(
                    title = "Nothing pinned yet",
                    subtitle = "Long-press any item to pin it."
                )
            }
        } else {
            when (displayMode) {
                NoteDisplayMode.LIST -> {
                    items(items = pinned, key = { "pinned_${it.note.id}" }) { item ->
                        NoteDisplayCard(
                            item = item,
                            mode = displayMode,
                            onClick = { onOpenNoteDetail(item.note.id) },
                            onLongPress = { selectedNoteId = item.note.id }
                        )
                    }
                }

                NoteDisplayMode.ICONS,
                NoteDisplayMode.GRID,
                NoteDisplayMode.BOOK -> {
                    item {
                        NotesGridBlock(
                            notes = pinned,
                            mode = displayMode,
                            onOpenNoteDetail = onOpenNoteDetail,
                            onLongPress = { selectedNoteId = it }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(10.dp)) }
    }

    if (showDisplaySheet) {
        DisplayModeSheet(
            currentMode = displayMode,
            onSelect = { mode ->
                scope.launch { uiPrefs.setDisplayMode(mode) }
            },
            onDismiss = { showDisplaySheet = false }
        )
    }

    if (selectedNote != null) {
        NoteActionSheet(
            title = selectedNote.title,
            isPinned = selectedNote.isPinned,
            moveTree = listOf(
                MoveScholar(
                    name = "Folders",
                    categories = listOf(
                        MoveCategory("General Notes", listOf("Inbox", "Highlights")),
                        MoveCategory("Projects", listOf("Ideas", "Research"))
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
private fun NotesGridBlock(
    notes: List<NoteUiItem>,
    mode: NoteDisplayMode,
    onOpenNoteDetail: (String) -> Unit,
    onLongPress: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        notes.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    NoteDisplayCard(
                        item = item,
                        mode = mode,
                        onClick = { onOpenNoteDetail(item.note.id) },
                        onLongPress = { onLongPress(item.note.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
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
    AppSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = "Search",
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StatsRow(
    notesCount: Int,
    pinnedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(label = "$notesCount notes")
        StatChip(label = "$pinnedCount pinned")
    }
}

@Composable
private fun StatChip(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun QuickActionsRow(
    onCreateNote: () -> Unit,
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
    val scale = if (pressed) 0.985f else 1f

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (pressed) 1.dp else 2.dp,
        shadowElevation = if (pressed) 1.dp else 4.dp
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyCard(
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
