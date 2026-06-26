package com.dinsoft.notes.ui.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.R
import com.dinsoft.notes.data.Attachment
import com.dinsoft.notes.data.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(note: Note?, onDismiss: () -> Unit, onSave: (Note, List<Attachment>) -> Unit) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var atts by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var picker by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    AlertDialog(onDismissRequest = onDismiss, modifier = Modifier.fillMaxWidth()) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.title_hint)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(content, { content = it }, label = { Text(stringResource(R.string.content_hint)) }, Modifier.fillMaxWidth().heightIn(min = 120.dp))
                Spacer(Modifier.height(8.dp))
                
                TextButton(onClick = { picker = true }) { Icon(Icons.Default.AttachFile, null); Text(stringResource(R.string.add_attachment)) }
                atts.forEach { a -> Text("📎 ${a.fileName}", style = MaterialTheme.typography.bodySmall) }
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onDismiss) { Text(stringResource(R.string.cancel)) }
                    Button(onClick = { onSave(Note(note?.id ?: 0, title, content), atts) }) { Text(stringResource(R.string.save)) }
                }
            }
        }
    }

    if (picker) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val name = uri.lastPathSegment ?: "file"
                atts = atts + Attachment(0, note?.id ?: 0, uri.toString(), name, "file")
                picker = false
            }
        }
        AlertDialog(onDismissRequest = { picker = false }, title = { Text(stringResource(R.string.add_attachment)) },
            text = {
                Column {
                    OutlinedButton({ launcher.launch("*/*") }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.attach_document)) }
                    OutlinedButton({ launcher.launch("image/*") }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.attach_photo)) }
                    OutlinedButton({ launcher.launch("video/*") }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.attach_video)) }
                    OutlinedButton({ launcher.launch("audio/*") }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.attach_music)) }
                }
            }, confirmButton = { TextButton({ picker = false }) { Text("OK") } })
    }
}