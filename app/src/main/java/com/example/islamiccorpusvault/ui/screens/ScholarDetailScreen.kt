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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.data.repo.ScholarCategory
import kotlinx.coroutines.launch

@Composable
fun ScholarDetailScreen(
    id: String,
    name: String,
    onBack: () -> Unit = {},
    onCategoryClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showNewCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val corpusRepository = AppContainer.corpusRepository
    val scope = rememberCoroutineScope()
    val categories by corpusRepository.observeCategoriesByScholar(id).collectAsState(initial = emptyList())

    val filtered = if (query.isBlank()) categories else categories.filter {
        it.name.contains(query, ignoreCase = true)
    }

    if (showNewCategory) {
        AlertDialog(
            onDismissRequest = { showNewCategory = false },
            title = { Text("New category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    singleLine = true,
                    label = { Text("Category name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newCategoryName.trim()
                        if (clean.isNotEmpty()) {
                            val base = clean.lowercase().replace(" ", "_")
                            var candidate = "${id}_${base}"
                            var i = 2
                            val existingIds = categories.map { it.id }.toSet()
                            while (candidate in existingIds) {
                                candidate = "${id}_${base}_${i}"
                                i++
                            }
                            scope.launch {
                                corpusRepository.upsertCategory(
                                    ScholarCategory(
                                        id = candidate,
                                        scholarId = id,
                                        name = clean
                                    )
                                )
                            }
                        }
                        newCategoryName = ""
                        showNewCategory = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = {
                    newCategoryName = ""
                    showNewCategory = false
                }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search within this scholar…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    title = "Add entry",
                    icon = Icons.Outlined.Add,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "New category",
                    icon = Icons.Outlined.Category,
                    onClick = { showNewCategory = true },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Pin",
                    icon = Icons.Outlined.PushPin,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(filtered, key = { it.id }) { cat ->
            CategoryCard(
                title = cat.name,
                subtitle = "Tap to view entries",
                onClick = { onCategoryClick(cat.name) }
            )
        }

        item { Spacer(modifier = Modifier.height(0.dp)) }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
