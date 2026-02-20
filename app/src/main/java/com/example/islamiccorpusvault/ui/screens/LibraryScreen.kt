package com.example.islamiccorpusvault.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.data.repo.NoteAttachment
import com.example.islamiccorpusvault.ui.components.AppSearchField
import com.example.islamiccorpusvault.ui.components.humanReadableBytes
import com.example.islamiccorpusvault.ui.util.resolveAttachmentOpenUri
import kotlinx.coroutines.launch

enum class AttachmentFilter {
    ALL,
    PDF,
    IMAGE
}

@Composable
fun LibraryScreen(
    onOpenGeneralNotes: () -> Unit,
    onOpenNote: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notes by AppContainer.notesRepository.observeAll().collectAsState(initial = emptyList())
    val attachments by AppContainer.notesRepository.observeAllAttachments().collectAsState(initial = emptyList())

    var filter by remember { mutableStateOf(AttachmentFilter.ALL) }
    var query by remember { mutableStateOf("") }

    val noteTitleById = remember(notes) {
        notes.associate { it.id to it.title.ifBlank { "Untitled" } }
    }

    val filtered = remember(attachments, filter, query) {
        attachments.filter { item ->
            val typeMatch = when (filter) {
                AttachmentFilter.ALL -> true
                AttachmentFilter.PDF -> item.mimeType.contains("pdf", ignoreCase = true)
                AttachmentFilter.IMAGE -> item.mimeType.startsWith("image/", ignoreCase = true)
            }
            val nameMatch = query.isBlank() || item.displayName.contains(query, ignoreCase = true)
            typeMatch && nameMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = "Search",
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == AttachmentFilter.ALL, onClick = { filter = AttachmentFilter.ALL }, label = { Text("All") })
            FilterChip(selected = filter == AttachmentFilter.PDF, onClick = { filter = AttachmentFilter.PDF }, label = { Text("PDFs") })
            FilterChip(selected = filter == AttachmentFilter.IMAGE, onClick = { filter = AttachmentFilter.IMAGE }, label = { Text("Images") })
        }

        if (filtered.isEmpty()) {
            Text(
                text = "No attachments found yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onOpenGeneralNotes) { Text("Open Notes") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id }) { item ->
                    AttachmentRow(
                        item = item,
                        noteTitle = noteTitleById[item.noteId].orEmpty(),
                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(resolveAttachmentOpenUri(context, item), item.mimeType.ifBlank { null })
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(intent) }
                                .onFailure { error ->
                                    scope.launch {
                                        val message = if (error is ActivityNotFoundException) {
                                            "No app found to open this file"
                                        } else {
                                            "Unable to open file"
                                        }
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                        },
                        onOpenNote = { onOpenNote(item.noteId) }
                    )
                }
                item { Spacer(modifier = Modifier.padding(bottom = 90.dp)) }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun AttachmentRow(
    item: NoteAttachment,
    noteTitle: String,
    onOpen: () -> Unit,
    onOpenNote: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = when {
                        item.mimeType.contains("pdf", ignoreCase = true) -> Icons.Outlined.PictureAsPdf
                        item.mimeType.startsWith("image/", ignoreCase = true) -> Icons.Outlined.Image
                        else -> Icons.Outlined.Description
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = humanReadableBytes(item.sizeBytes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Note: ${noteTitle.ifBlank { "Untitled" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onOpen) { Text("Open") }
                TextButton(onClick = onOpenNote) { Text("Go to note") }
            }
        }
    }
}
