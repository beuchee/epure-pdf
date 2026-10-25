package com.alain.epure.pdf.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alain.epure.pdf.R
import com.alain.epure.pdf.domain.RecentDocument
import com.alain.epure.pdf.system.OpenError
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    error: OpenError?,
    recentUnavailable: Boolean,
    recent: RecentDocument?,
    onOpen: (Uri) -> Unit,
    onOpenRecent: () -> Unit,
    onRemoveRecent: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // Cancelling the picker returns null: stay on the home screen, without a message (SPEC F-1.1).
        if (uri != null) onOpen(uri)
    }
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            // Edge-to-edge: keep the title clear of the status bar and the navigation bar.
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = AppNameStyle,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        ActionRow(text = stringResource(R.string.open_file)) {
            picker.launch(arrayOf("application/pdf"))
        }
        recent?.let {
            Spacer(Modifier.height(24.dp))
            RecentEntry(
                document = it,
                onClick = onOpenRecent,
                onLongClick = { menuOpen = true }
            )
        }
        val message = when {
            recentUnavailable -> R.string.error_recent_unavailable
            error != null -> errorMessage(error)
            else -> null
        }
        message?.let {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(it),
                style = LabelStyle,
                color = TextSecondary
            )
        }
    }

    if (menuOpen && recent != null) {
        // Menu window (CHARTE A.6): the title is the file name, the action removes the entry only.
        Dialog(onDismissRequest = { menuOpen = false }) {
            Column(
                modifier = Modifier
                    .background(Black)
                    .padding(24.dp)
            ) {
                Text(
                    text = recent.name,
                    style = LabelStyle,
                    color = TextPrimary
                )
                Spacer(Modifier.height(24.dp))
                ActionRow(text = stringResource(R.string.remove_recent)) {
                    menuOpen = false
                    onRemoveRecent()
                }
            }
        }
    }
}

// The last document: name on the first line, page on the second, both as text only (SPEC F-4.1).
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun RecentEntry(document: RecentDocument, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Text(
            text = document.name,
            style = LabelStyle,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.recent_page, document.page + 1, document.pageCount),
            style = LabelStyle,
            color = TextSecondary
        )
    }
}

private fun errorMessage(error: OpenError): Int = when (error) {
    OpenError.NotFound -> R.string.error_not_found
    OpenError.AccessRevoked -> R.string.error_access_revoked
    OpenError.PasswordRequired -> R.string.error_password_required
    OpenError.Unreadable -> R.string.error_unreadable
}
