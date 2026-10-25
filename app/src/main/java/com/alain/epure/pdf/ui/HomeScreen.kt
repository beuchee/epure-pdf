package com.alain.epure.pdf.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.alain.epure.pdf.system.shareDocument
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    error: OpenError?,
    recentUnavailable: Boolean,
    recents: List<RecentDocument>,
    keepList: Boolean,
    onOpen: (Uri) -> Unit,
    onOpenRecent: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onSettings: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // Cancelling the picker returns null: stay on the home screen, without a message (SPEC F-1.1).
        if (uri != null) onOpen(uri)
    }
    val context = LocalContext.current
    // The document whose long-press menu is open.
    var menuFor by remember { mutableStateOf<RecentDocument?>(null) }
    // Set when a document could not be shared (SPEC F-12.2).
    var shareUnavailable by remember { mutableStateOf(false) }

    // Without the optional list, only the last document is shown (DEC-0024).
    val shown = if (keepList) recents else recents.take(1)

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
        if (shareUnavailable) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.share_unavailable),
                style = LabelStyle,
                color = TextSecondary
            )
        }
        Spacer(Modifier.height(24.dp))
        // The list takes the remaining height and scrolls when it is long.
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (shown.isEmpty()) {
                if (keepList) {
                    item {
                        Text(
                            text = stringResource(R.string.no_recent),
                            style = LabelStyle,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(shown, key = { it.uri }) { document ->
                    RecentEntry(
                        document = document,
                        onClick = { onOpenRecent(document.uri) },
                        onLongClick = { menuFor = document }
                    )
                }
            }
        }
        TextAction(text = stringResource(R.string.settings_title), onClick = onSettings)
    }

    menuFor?.let { document ->
        // Menu window (CHARTE A.6): the title is the file name, the action removes the entry only.
        Dialog(onDismissRequest = { menuFor = null }) {
            Column(
                modifier = Modifier
                    .background(Black)
                    .padding(24.dp)
            ) {
                Text(
                    text = document.name,
                    style = LabelStyle,
                    color = TextPrimary
                )
                Spacer(Modifier.height(24.dp))
                ActionRow(text = stringResource(R.string.share)) {
                    menuFor = null
                    shareUnavailable = !shareDocument(context, Uri.parse(document.uri), document.name)
                }
                ActionRow(text = stringResource(R.string.remove_recent)) {
                    menuFor = null
                    onRemoveRecent(document.uri)
                }
            }
        }
    }
}

// A recent document: name on the first line, page on the second, both as text only (SPEC F-4.1).
@OptIn(ExperimentalFoundationApi::class)
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
    OpenError.WrongPassword -> R.string.password_wrong
    OpenError.Unreadable -> R.string.error_unreadable
}
