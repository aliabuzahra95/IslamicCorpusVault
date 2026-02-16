package com.example.islamiccorpusvault.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.model.Scholar
import com.example.islamiccorpusvault.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun ScholarsScreen(navController: NavController) {
    val corpusRepository = AppContainer.corpusRepository
    val scope = rememberCoroutineScope()
    val scholars by corpusRepository.observeScholars().collectAsState(initial = emptyList())

    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var era by remember { mutableStateOf("") }
    var madhhab by remember { mutableStateOf("") }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New scholar") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = era,
                        onValueChange = { era = it },
                        label = { Text("Era (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = madhhab,
                        onValueChange = { madhhab = it },
                        label = { Text("Madhhab (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        if (trimmedName.isNotEmpty()) {
                            val baseId = slugify(trimmedName)
                            var candidate = baseId
                            var i = 2
                            while (scholars.any { it.id == candidate }) {
                                candidate = "${baseId}_${i}"
                                i++
                            }

                            scope.launch {
                                corpusRepository.upsertScholar(
                                    Scholar(
                                        id = candidate,
                                        name = trimmedName,
                                        era = era.trim().takeIf { it.isNotEmpty() },
                                        madhhab = madhhab.trim().takeIf { it.isNotEmpty() }
                                    )
                                )
                            }
                        }

                        name = ""
                        era = ""
                        madhhab = ""
                        showAdd = false
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        name = ""
                        era = ""
                        madhhab = ""
                        showAdd = false
                    }
                ) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Scholars",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdd = true }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "+ Add a new scholar",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Create your own list",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        items(items = scholars, key = { it.id }) { scholar ->
            ScholarRow(
                scholar = scholar,
                onClick = {
                    navController.navigate(
                        "${Routes.SCHOLAR_DETAIL}/${Uri.encode(scholar.id)}/${Uri.encode(scholar.name)}"
                    )
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ScholarRow(
    scholar: Scholar,
    onClick: () -> Unit
) {
    val meta = listOfNotNull(scholar.era, scholar.madhhab).joinToString(" • ")

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = scholar.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (meta.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun slugify(input: String): String {
    val lowered = input.lowercase()
    val sb = StringBuilder()
    var lastUnderscore = false

    for (ch in lowered) {
        val ok = (ch in 'a'..'z') || (ch in '0'..'9')
        if (ok) {
            sb.append(ch)
            lastUnderscore = false
        } else {
            if (!lastUnderscore) {
                sb.append('_')
                lastUnderscore = true
            }
        }
    }

    return sb.toString().trim('_').ifBlank { "scholar" }
}
