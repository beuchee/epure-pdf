package com.alain.epure.pdf.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.pdf.R
import com.alain.epure.pdf.system.OpenError
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary

@Composable
fun HomeScreen(error: OpenError?, onOpen: (Uri) -> Unit) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // Cancelling the picker returns null: stay on the home screen, without a message (SPEC F-1.1).
        if (uri != null) {
            // Keep read access so the file can be reopened later (SPEC F-1.1, used by session 2b).
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                // The file still opens for this session; only reopening later is affected.
            }
            onOpen(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = AppNameStyle,
            color = TextPrimary
        )
        Spacer(Modifier.height(24.dp))
        ActionRow(text = stringResource(R.string.open_file)) {
            picker.launch(arrayOf("application/pdf"))
        }
        error?.let {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(errorMessage(it)),
                style = LabelStyle,
                color = TextSecondary
            )
        }
    }
}

private fun errorMessage(error: OpenError): Int = when (error) {
    OpenError.NotFound -> R.string.error_not_found
    OpenError.AccessRevoked -> R.string.error_access_revoked
    OpenError.PasswordRequired -> R.string.error_password_required
    OpenError.Unreadable -> R.string.error_unreadable
}
