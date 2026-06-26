// app/src/main/java/com/dinsoft/notes/ui/component/NoteDialog.kt
package com.dinsoft.notes.ui.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.R
import com.dinsoft.notes.data.Attachment
import com.dinsoft.notes.data.AttachmentType
import com.dinsoft.notes.data.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (Note, List<Attachment>) -> Unit,
    existingAttachments: List<Attachment> = emptyList()
) {
    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }
    var showAttachmentPicker by remember { mutableStateOf(false) }
    var attachments by remember(note) { mutableStateOf(existingAttachments) }  // ← Inisialisasi dengan existing
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (note == null) stringResource(R.string.new_note) 
                           else stringResource(R.string.edit_note),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(stringResource(R.string.content_hint)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 200.dp),
                    maxLines = 8
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Attachment Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.attachments), style = MaterialTheme.typography.labelLarge)
                    FilledTonalButton(onClick = { showAttachmentPicker = true }) {
                        Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.add_attachment))
                    }
                }
                
                // Tampilkan attachments
                if (attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    attachments.forEach { attachment ->
                        AttachmentItem(
                            attachment = attachment,
                            onRemove = { attachments = attachments - attachment }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (title.isNotBlank() || content.isNotBlank() || attachments.isNotEmpty()) {
                            val newNote = Note(
                                id = note?.id ?: 0,
                                title = title,
                                content = content,
                                timestamp = System.currentTimeMillis()
                            )
                            onSave(newNote, attachments)  // ← Kirim note + attachments
                        }
                    }) { Text(stringResource(R.string.save)) }
                }
            }
        }
    }
    
    // Attachment Picker
    if (showAttachmentPicker) {
        AttachmentPickerDialog(
            onDismiss = { showAttachmentPicker = false },
            onFileSelected = { attachment ->
                attachments = attachments + attachment
                showAttachmentPicker = false
            }
        )
    }
}

@Composable
fun AttachmentItem(attachment: Attachment, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = when (attachment.type) {
                        AttachmentType.DOCUMENT -> Icons.Default.Description
                        AttachmentType.PHOTO -> Icons.Default.Image
                        AttachmentType.VIDEO -> Icons.Default.Videocam
                        AttachmentType.MUSIC -> Icons.Default.MusicNote
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = attachment.fileName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Text(
                        text = formatFileSize(attachment.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, stringResource(R.string.remove_attachment), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AttachmentPickerDialog(
    onDismiss: () -> Unit,
    onFileSelected: (Attachment) -> Unit
) {
    val context = LocalContext.current
    
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFileSelected(createAttachmentFromUri(context, it, AttachmentType.DOCUMENT)) }
    }
    
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFileSelected(createAttachmentFromUri(context, it, AttachmentType.PHOTO)) }
    }
    
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFileSelected(createAttachmentFromUri(context, it, AttachmentType.VIDEO)) }
    }
    
    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFileSelected(createAttachmentFromUri(context, it, AttachmentType.MUSIC)) }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_attachment)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { documentPicker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Description, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.attach_document))
                }
                OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Image, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.attach_photo))
                }
                OutlinedButton(onClick = { videoPicker.launch("video/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Videocam, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.attach_video))
                }
                OutlinedButton(onClick = { musicPicker.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.MusicNote, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.attach_music))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

// Helper functions
fun createAttachmentFromUri(context: android.content.Context, uri: android.net.Uri, type: AttachmentType): Attachment {
    var fileName = "unknown"
    var mimeType = "unknown"
    var size = 0L
    
    // Copy file ke internal storage
    val savedUri = copyToInternalStorage(context, uri)
    
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (nameIndex >= 0) fileName = it.getString(nameIndex) ?: "unknown"
            if (sizeIndex >= 0) size = it.getLong(sizeIndex)
        }
    }
    mimeType = context.contentResolver.getType(uri) ?: "unknown"
    
    return Attachment(
        id = 0,
        noteId = 0,
        uri = savedUri?.toString() ?: uri.toString(),
        fileName = fileName,
        mimeType = mimeType,
        size = size,
        type = type
    )
}

// Copy file ke internal storage agar tetap ada
fun copyToInternalStorage(context: android.content.Context, uri: android.net.Uri): android.net.Uri? {
    try {
        val dir = java.io.File(context.filesDir, "attachments")
        if (!dir.exists()) dir.mkdirs()
        
        val fileName = "${System.currentTimeMillis()}_${uri.lastPathSegment ?: "file"}"
        val file = java.io.File(dir, fileName)
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            java.io.FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        
        return android.net.Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}