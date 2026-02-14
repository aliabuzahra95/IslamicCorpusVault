package com.example.islamiccorpusvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

private data class SubcategoryItem(val id: String, val name: String)
private data class NoteItem(val id: String, val title: String, val body: String, val citation: String)

private enum class SheetMode { ACTIONS, NEW_SUBCATEGORY, NEW_NOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    scholarName: String,
    categoryName: String,
    onCreateSubcategory: () -> Unit,
    onCreateNote: () -> Unit,
    onSubcategoryClick: (subcategoryId: String, subcategoryName: String) -> Unit,
    onNoteClick: (noteId: String, title: String, body: String, citation: String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var sheetMode by remember { mutableStateOf(SheetMode.ACTIONS) }

    val subcategories = remember { mutableStateListOf<SubcategoryItem>() }
    val notes = remember { mutableStateListOf<NoteItem>() }

    // form state
    var newSubcategoryName by remember { mutableStateOf("") }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteBody by remember { mutableStateOf("") }
    var newNoteCitation by remember { mutableStateOf("") }

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
                }

                if (notes.isEmpty()) {
                    item {
                        Text(
                            text = "No notes yet. Tap + and create one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(items = notes, key = { it.id }) { note ->
                        NoteCard(
                            title = note.title,
                            body = note.body,
                            citation = note.citation,
                            onClick = { onNoteClick(note.id, note.title, note.body, note.citation) }
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
                            subtitle = "Write directly here",
                            icon = Icons.Outlined.NoteAdd,
                            onClick = {
                                sheetMode = SheetMode.NEW_NOTE
                            }
                        )

                        Spacer(Modifier.height(14.dp))
                    }
                }

                SheetMode.NEW_SUBCATEGORY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                        subcategories.add(SubcategoryItem(id = System.currentTimeMillis().toString(), name = name))
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

                SheetMode.NEW_NOTE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("New note", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newNoteTitle,
                            onValueChange = { newNoteTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Title") },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newNoteBody,
                            onValueChange = { newNoteBody = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            placeholder = { Text("Body text") },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newNoteCitation,
                            onValueChange = { newNoteCitation = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            placeholder = { Text("Citation (optional)") },
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
                                    val t = newNoteTitle.trim()
                                    val b = newNoteBody.trim()
                                    if (t.isNotEmpty() || b.isNotEmpty()) {
                                        notes.add(
                                            NoteItem(
                                                id = System.currentTimeMillis().toString(),
                                                title = if (t.isNotEmpty()) t else "Untitled",
                                                body = b,
                                                citation = newNoteCitation.trim()
                                            )
                                        )
                                        newNoteTitle = ""
                                        newNoteBody = ""
                                        newNoteCitation = ""
                                        showSheet = false
                                        sheetMode = SheetMode.ACTIONS
                                        onCreateNote()
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

@Composable
private fun NoteCard(
    title: String,
    body: String,
    citation: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
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
        }
    }
}
