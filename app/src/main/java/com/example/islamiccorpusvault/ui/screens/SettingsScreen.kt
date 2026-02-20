package com.example.islamiccorpusvault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.islamiccorpusvault.data.backup.BackupManager
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.components.DisplayModeSheet
import com.example.islamiccorpusvault.ui.settings.NoteDisplayMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiPrefs = AppContainer.uiPrefsRepository
    val backupManager = remember {
        BackupManager(
            context = context.applicationContext,
            db = AppContainer.database(),
            uiPrefsRepository = uiPrefs
        )
    }

    var isWorking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showDisplaySheet by remember { mutableStateOf(false) }

    val displayMode by uiPrefs.displayModeFlow.collectAsState(initial = NoteDisplayMode.LIST)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isWorking = true
            statusMessage = null
            try {
                withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openOutputStream(uri)
                        ?: error("Unable to open destination file.")
                    stream.use { backupManager.exportTo(it) }
                }
                statusMessage = "Backup exported successfully."
            } catch (t: Throwable) {
                statusMessage = "Export failed: ${t.message ?: "Unknown error"}"
            } finally {
                isWorking = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDisplaySheet = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Note display",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = displayMode.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Backup & Restore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Exports EVERYTHING (folders, notes, tags, attachments, and app settings) into a single ZIP backup. Import replaces current data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val fileName = "islamic-corpus-vault-backup-${System.currentTimeMillis()}.zip"
                            exportLauncher.launch(fileName)
                        },
                        enabled = !isWorking
                    ) {
                        Text("Export Backup")
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                        enabled = !isWorking
                    ) {
                        Text("Import Backup")
                    }
                }

                if (isWorking) {
                    Text(
                        text = "Working...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!statusMessage.isNullOrBlank()) {
                    Text(
                        text = statusMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }

    if (showDisplaySheet) {
        DisplayModeSheet(
            currentMode = displayMode,
            onSelect = { mode -> scope.launch { uiPrefs.setDisplayMode(mode) } },
            onDismiss = { showDisplaySheet = false }
        )
    }

    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Replace all data?") },
            text = {
                Text("Import will delete current local data and restore from the selected backup file. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingImportUri ?: return@TextButton
                        pendingImportUri = null
                        scope.launch {
                            isWorking = true
                            statusMessage = null
                            try {
                                withContext(Dispatchers.IO) {
                                    val stream = context.contentResolver.openInputStream(uri)
                                        ?: error("Unable to open selected file.")
                                    stream.use { backupManager.importFrom(it) }
                                }
                                statusMessage = "Backup imported successfully."
                            } catch (t: Throwable) {
                                statusMessage = "Import failed: ${t.message ?: "Invalid backup file."}"
                            } finally {
                                isWorking = false
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
