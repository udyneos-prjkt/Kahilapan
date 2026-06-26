package com.dinsoft.notes.ui.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dinsoft.notes.R
import com.dinsoft.notes.viewmodel.NoteViewModel

@Composable
fun BackupDialog(vm: NoteViewModel, onDismiss: () -> Unit) {
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let { vm.backup(it); onDismiss() }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.restore(it); onDismiss() }
    }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.backup_restore)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ backupLauncher.launch(vm.backupName()) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.backup_notes)) }
                OutlinedButton({ restoreLauncher.launch(arrayOf("application/zip", "*/*")) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.restore_notes)) }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("Close") } })
}