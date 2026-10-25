package com.alain.epure.pdf.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alain.epure.pdf.R
import com.alain.epure.pdf.domain.nextOccurrence
import com.alain.epure.pdf.domain.previousOccurrence
import com.alain.epure.pdf.system.PageRect
import com.alain.epure.pdf.system.PdfDocument
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.NoteStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// One occurrence of the search and the page it is on (SPEC F-11.3).
class SearchHit(val page: Int, val rects: List<PageRect>)

sealed interface SearchStatus {
    data object Idle : SearchStatus
    data object Searching : SearchStatus
    class Found(val hits: List<SearchHit>) : SearchStatus
    data object NotFound : SearchStatus
    data object NoText : SearchStatus
}

// The search of the open document (SPEC F-11). It belongs to the reader: closing the document ends it.
class ReaderSearch(private val document: PdfDocument) {
    var status by mutableStateOf<SearchStatus>(SearchStatus.Idle)
        private set

    var current by mutableIntStateOf(0)
        private set

    private var job: Job? = null

    val active: Boolean get() = status != SearchStatus.Idle

    // Page by page, so that « Retour » can interrupt it (SPEC F-11.5). The result appears at the end.
    fun start(scope: CoroutineScope, text: String) {
        val query = text.trim()
        if (query.isEmpty()) return
        job?.cancel()
        status = SearchStatus.Searching
        current = 0
        job = scope.launch {
            val hits = mutableListOf<SearchHit>()
            for (page in 0 until document.pageCount) {
                document.searchPage(page, query)?.forEach { hits += SearchHit(page, it) }
            }
            status = when {
                hits.isNotEmpty() -> SearchStatus.Found(hits)
                anyPageHasText() -> SearchStatus.NotFound
                else -> SearchStatus.NoText
            }
        }
    }

    fun next() {
        val found = status as? SearchStatus.Found ?: return
        current = nextOccurrence(current, found.hits.size)
    }

    fun previous() {
        val found = status as? SearchStatus.Found ?: return
        current = previousOccurrence(current, found.hits.size)
    }

    fun close() {
        job?.cancel()
        job = null
        status = SearchStatus.Idle
        current = 0
    }

    // Page of the occurrence on show, to scroll to.
    val currentPage: Int?
        get() = (status as? SearchStatus.Found)?.hits?.getOrNull(current)?.page

    // Every occurrence on the page is marked, not only the one on show (SPEC F-11.4).
    fun highlightsOn(page: Int): List<PageRect> =
        (status as? SearchStatus.Found)?.hits?.filter { it.page == page }?.flatMap { it.rects }.orEmpty()

    // A scanned document has no text to find: this tells « no text » from « text not found » (SPEC F-11.6).
    private suspend fun anyPageHasText(): Boolean =
        (0 until document.pageCount).any { document.hasText(it) }
}

// Bottom bar while a search is open: its state on the first line, the steps and closing on the second (SPEC F-11.3).
@Composable
fun SearchBar(search: ReaderSearch, modifier: Modifier = Modifier) {
    val status = search.status
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp)
    ) {
        val label = when (status) {
            SearchStatus.Idle, SearchStatus.Searching -> stringResource(R.string.search_searching)
            is SearchStatus.Found -> stringResource(R.string.search_counter, search.current + 1, status.hits.size)
            SearchStatus.NotFound -> stringResource(R.string.search_not_found)
            SearchStatus.NoText -> stringResource(R.string.search_no_text)
        }
        Text(
            text = label,
            style = LabelStyle,
            color = TextPrimary,
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status is SearchStatus.Found) {
                TextAction(text = stringResource(R.string.search_previous)) { search.previous() }
                Spacer(Modifier.width(24.dp))
                TextAction(text = stringResource(R.string.search_next)) { search.next() }
                Spacer(Modifier.width(24.dp))
            }
            TextAction(text = stringResource(R.string.search_close)) { search.close() }
        }
    }
}

// « Rechercher » window (SPEC F-11.1): the field opens with the keyboard, Entrée starts the search.
@Composable
fun SearchDialog(searchable: Boolean, onSearch: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }

    fun submit() {
        if (searchable) onSearch(text)
    }

    LaunchedEffect(Unit) { focus.requestFocus() }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Black)
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.search_title),
                style = LabelStyle,
                color = TextPrimary
            )
            Spacer(Modifier.height(24.dp))
            if (searchable) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = AppNameStyle.copy(color = TextPrimary),
                    cursorBrush = SolidColor(TextPrimary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .focusRequester(focus)
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    TextAction(text = stringResource(R.string.cancel), onClick = onDismiss)
                    TextAction(text = stringResource(R.string.search_title), onClick = { submit() })
                }
            } else {
                Text(
                    text = stringResource(R.string.search_unavailable),
                    style = NoteStyle,
                    color = TextSecondary
                )
                Spacer(Modifier.height(24.dp))
                TextAction(text = stringResource(R.string.cancel), onClick = onDismiss)
            }
        }
    }
}
