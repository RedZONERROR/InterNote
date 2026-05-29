package com.example.ui.settings

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.main.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val exportUri by viewModel.exportEvent.collectAsStateWithLifecycle()
    val resetState by viewModel.resetAppEvent.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }

    // 1. Listen for ZIP Export Completion and trigger Android Share chooser
    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save Notes Portfolio ZIP"))
            viewModel.clearExportEvent()
            Toast.makeText(context, "Secure backup archive compiled successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Listen for Right to Be Forgotten Purges and close APP
    LaunchedEffect(resetState) {
        if (resetState) {
            Toast.makeText(context, "All database, cache, and key vaults overwritten cleanly", Toast.LENGTH_LONG).show()
            delayAndTerminate(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & GDPR Privacy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Dashboard")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Local-First Security Overview Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Security Shield Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Local-First Architecture",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your notes list never leaves this device. Data is encrypted at rest using high-resistance SQLCipher encryption.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Section: GDPR Portability (Backup & Export)
            Text(
                "GDPR DATA PORTABILITY",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Backup & Portability Suite (Art. 20 GDPR)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Export a ZIP bundle containing all of your folder schemas, tags, and notes formatted cleanly as individual standard Markdown (.md) and JSON documents.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.performExport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_backup_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DownloadForOffline, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compile & Export Data ZIP", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Section: Right to Be Forgotten
            Text(
                "GDPR RIGHT TO BE FORGOTTEN",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Red,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Right to Erasure (Art. 17 GDPR)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Completely override, wipe, and purge the local encrypted database, secure credentials, key containers, and persistent settings instantly. This operation is strictly final.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wipe_db_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DeleteSweep, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wipe Database & Reset App", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Section: EU Accessibility Act & Compliance
            Text(
                "REGULATORY STANDARDS (EAA)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RegulatoryBulletCard(
                    icon = Icons.Outlined.AccessibilityNew,
                    title = "EU Accessibility Act (EAA) Compliant",
                    desc = "Screen readers fully supported via explicit content annotations. Minimum interaction margins are strictly dimensioned above 48dp x 48dp."
                )
                RegulatoryBulletCard(
                    icon = Icons.Outlined.Lock,
                    title = "Database Decoupled & Offline-First",
                    desc = "Wording and designs correspond with security requirements of EN 301 549, and digital private safe regulations."
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "Inter Note app version 1.0.0 (EU-M3 PRO)\nTailored for secure standalone environments",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // RIGHT TO BE FORGOTTEN CONFIRMATION DIALOGUE
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Filled.DeleteSweep, "Purge warning", tint = MaterialTheme.colorScheme.error) },
            title = { Text("Confirm Clear SQLite Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You are about to exercise your Right to Erasure (GDPR Art. 17). This will securely overwrite your files, invalidate your crypt keys, and purge all offline data. This process cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.wipeAllUserData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Overwrite & Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RegulatoryBulletCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, fontSize = 11.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private suspend fun delayAndTerminate(context: android.content.Context) {
    kotlinx.coroutines.delay(1800)
    (context as? Activity)?.finishAffinity()
}
