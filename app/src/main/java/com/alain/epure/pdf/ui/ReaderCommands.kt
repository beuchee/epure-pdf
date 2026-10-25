package com.alain.epure.pdf.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alain.epure.pdf.R
import com.alain.epure.pdf.domain.parsePageNumber
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.NoteStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary

// Top of the reader's commands: the file name, text only (SPEC F-6.3).
@Composable
fun CommandsTop(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        style = LabelStyle,
        color = TextPrimary,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

// Bottom of the reader's commands: the page indicator (opens « Aller à la page ») and « Plus » (SPEC F-6.3, F-7.1).
@Composable
fun CommandsBottom(
    page: Int,
    pageCount: Int,
    onPageClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.page_indicator, page, pageCount),
            style = LabelStyle,
            color = TextSecondary,
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .clickable(onClick = onPageClick)
        )
        Spacer(Modifier.weight(1f))
        TextAction(text = stringResource(R.string.more), onClick = onMoreClick)
    }
}

// « Plus » window (CHARTE A.6): the less frequent actions. Returns false from onShare when the document cannot be shared.
@Composable
fun MoreMenu(
    bookmarked: Boolean,
    hasBookmarks: Boolean,
    onToggleBookmark: () -> Unit,
    onShowBookmarks: () -> Unit,
    nightMode: Boolean,
    onToggleNightMode: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onShare: () -> Boolean,
    onDismiss: () -> Unit
) {
    var shareUnavailable by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Black)
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.more),
                style = LabelStyle,
                color = TextPrimary
            )
            Spacer(Modifier.height(24.dp))
            ActionRow(text = stringResource(R.string.share)) {
                shareUnavailable = !onShare()
            }
            if (shareUnavailable) {
                Text(
                    text = stringResource(R.string.share_unavailable),
                    style = LabelStyle,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(24.dp))
            // Title, then the action in grey below it: « Ajouter » becomes « Retirer » once the page is marked.
            ActionRow(text = stringResource(R.string.bookmark_title)) { onToggleBookmark() }
            TextAction(text = stringResource(if (bookmarked) R.string.bookmark_remove else R.string.bookmark_add)) {
                onToggleBookmark()
            }
            // Opens the bookmarks window; a long press there removes a bookmark (SPEC F-10.3).
            TextAction(text = stringResource(if (hasBookmarks) R.string.bookmarks else R.string.no_bookmark)) {
                onShowBookmarks()
            }
            Spacer(Modifier.height(24.dp))
            ActionRow(text = stringResource(R.string.night_mode)) { onToggleNightMode(!nightMode) }
            Text(
                text = stringResource(if (nightMode) R.string.state_on else R.string.state_off),
                style = LabelStyle,
                color = TextSecondary
            )
            Spacer(Modifier.height(24.dp))
            ActionRow(text = stringResource(R.string.keep_screen_on)) { onToggleKeepScreenOn(!keepScreenOn) }
            Text(
                text = stringResource(if (keepScreenOn) R.string.state_on else R.string.state_off),
                style = LabelStyle,
                color = TextSecondary
            )
        }
    }
}

// « Marque-pages » window (SPEC F-10.3): a tap goes to the page, a long press removes the bookmark at once (DEC-0035).
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarksDialog(pages: List<Int>, onGo: (Int) -> Unit, onRemove: (Int) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Black)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.bookmarks),
                style = LabelStyle,
                color = TextPrimary
            )
            Spacer(Modifier.height(24.dp))
            if (pages.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_bookmark),
                    style = LabelStyle,
                    color = TextSecondary
                )
            }
            pages.forEach { page ->
                Text(
                    text = stringResource(R.string.bookmark_page, page + 1),
                    style = AppNameStyle,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .combinedClickable(onClick = { onGo(page) }, onLongClick = { onRemove(page) })
                )
            }
        }
    }
}

// « Aller à la page » (SPEC F-7.2): a number field; an invalid number keeps the window open with a message.
@Composable
fun GoToPageDialog(pageCount: Int, onDismiss: () -> Unit, onGo: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }

    fun submit() {
        val page = parsePageNumber(text, pageCount)
        if (page == null) invalid = true else onGo(page)
    }

    LaunchedEffect(Unit) { focus.requestFocus() }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Black)
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.go_to_page),
                style = LabelStyle,
                color = TextPrimary
            )
            Spacer(Modifier.height(24.dp))
            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    invalid = false
                },
                textStyle = AppNameStyle.copy(color = TextPrimary),
                cursorBrush = SolidColor(TextPrimary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { submit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .focusRequester(focus)
            )
            if (invalid) {
                Text(
                    text = stringResource(R.string.page_out_of_range, pageCount),
                    style = NoteStyle,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                TextAction(text = stringResource(R.string.cancel), onClick = onDismiss)
                TextAction(text = stringResource(R.string.go), onClick = { submit() })
            }
        }
    }
}
