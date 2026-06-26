// app/src/main/java/com/dinsoft/notes/ui/component/AttachmentPicker.kt
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
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.data.Attachment
import com.dinsoft.notes.data.AttachmentType

@Composable
fun AttachmentPicker(
    noteId: Int,
    onAttachmentSelected: (Attachment) -> Unit
) {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    
    // Document Picker
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val attachment = createAttachment(context, it, AttachmentType.DOCUMENT, noteId)
            onAttachmentSelected(attachment)
        }
    }
    
    // Photo Picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val attachment = createAttachment(context, it, AttachmentType.PHOTO, noteId)
            onAttachmentSelected(attachment)
        }
    }
    
    // Video Picker
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val attachment = createAttachment(context, it, AttachmentType.VIDEO, noteId)
            onAttachmentSelected(attachment)
        }
    }
    
    // Music Picker
    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val attachment = createAttachment(context, it, AttachmentType.MUSIC, noteId)
            onAttachmentSelected(attachment)
        }
    }
    
    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Add Attachment") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Document
                    OutlinedButton(
                        onClick = {
                            documentPicker.launch("*/*")
                            showPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Description, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Document")
                    }
                    
                    // Photo
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch("image/*")
                            showPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Photo")
                    }
                    
                    // Video
                    OutlinedButton(
                        onClick = {
                            videoPicker.launch("video/*")
                            showPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Videocam, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Video")
                    }
                    
                    // Music
                    OutlinedButton(
                        onClick = {
                            musicPicker.launch("audio/*")
                            showPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MusicNote, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Music")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun createAttachment(
    context: android.content.Context,
    uri: Uri,
    type: AttachmentType,
    noteId: Int
): Attachment {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    var fileName = "unknown"
    var mimeType = "unknown"
    var size = 0L
    
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
        noteId = noteId,
        uri = uri.toString(),
        fileName = fileName,
        mimeType = mimeType,
        size = size,
        type = type
    )
}