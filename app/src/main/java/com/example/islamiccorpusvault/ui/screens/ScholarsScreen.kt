package com.example.islamiccorpusvault.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.islamiccorpusvault.ui.model.Scholar
import com.example.islamiccorpusvault.ui.navigation.Routes

@Composable
fun ScholarsScreen(navController: NavController) {
    val scholars = remember {
        mutableStateListOf(
            Scholar(id = "ibn_taymiyyah", name = "Ibn Taymiyyah", era = "661–728H", madhhab = "Hanbali"),
            Scholar(id = "ibn_al_qayyim", name = "Ibn al-Qayyim", era = "691–751H", madhhab = "Hanbali"),
            Scholar(id = "ahmad_ibn_hanbal", name = "Ahmad ibn Hanbal", era = "164–241H", madhhab = "Hanbali"),
            Scholar(id = "al_bukhari", name = "Al-Bukhari", era = "194–256H", madhhab = null)
        )
    }

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
                            val finalEra = era.trim().takeIf { it.isNotEmpty() }
                            val finalMadhhab = madhhab.trim().takeIf { it.isNotEmpty() }

                            var candidate = baseId
                            var i = 2
                            while (scholars.any { it.id == candidate }) {
                                candidate = "${baseId}_${i}"
                                i++
                            }

                            scholars.add(
                                Scholar(
                                    id = candidate,
                                    name = trimmedName,
                                    era = finalEra,
                                    madhhab = finalMadhhab
                                )
                            )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Scholars",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                    text = "Create your own list (local for now)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        scholars.forEach { scholar ->
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