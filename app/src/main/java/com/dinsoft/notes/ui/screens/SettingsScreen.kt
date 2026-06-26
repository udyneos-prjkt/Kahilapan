// app/src/main/java/com/dinsoft/notes/ui/screens/SettingsScreen.kt
package com.dinsoft.notes.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.R
import com.dinsoft.notes.ui.component.AboutDeveloperDialog
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguage: StateFlow<String>  // ← Gunakan StateFlow
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val language by currentLanguage.collectAsState()  // ← Collect StateFlow
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // SECTION: GENERAL
            SettingsSection(title = stringResource(R.string.general)) {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subtitle = if (language == "en") stringResource(R.string.english) else stringResource(R.string.indonesian),
                    onClick = { showLanguageDialog = true }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.theme),
                    subtitle = "Material You (Auto)",
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // SECTION: DATA
            SettingsSection(title = stringResource(R.string.data_storage)) {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = stringResource(R.string.backup_restore),
                    subtitle = stringResource(R.string.backup_desc),
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // SECTION: ABOUT
            SettingsSection(title = stringResource(R.string.about)) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about_developer),
                    subtitle = stringResource(R.string.about_developer_desc),
                    onClick = { showAboutDialog = true }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.rate_app),
                    subtitle = stringResource(R.string.rate_app_desc),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=com.dinsoft.notes")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.share_app),
                    subtitle = stringResource(R.string.share_app_desc),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Download Kahilapan Notes App: https://play.google.com/store/apps/details?id=com.dinsoft.notes")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                    }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = stringResource(R.string.privacy_policy),
                    subtitle = stringResource(R.string.privacy_policy_desc),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://dinsoft.com/privacy")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.version),
                    subtitle = "1.0.0 (Build 1)",
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer
            Text(
                text = "Kahilapan © ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
    
    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == "en",
                            onClick = {
                                onLanguageChange("en")
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.english))
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("in")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == "in",
                            onClick = {
                                onLanguageChange("in")
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.indonesian))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AboutDeveloperDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}