package com.alain.epure.pdf.ui

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alain.epure.pdf.AppContainer
import com.alain.epure.pdf.domain.RecentDocument
import com.alain.epure.pdf.domain.isSameFile
import com.alain.epure.pdf.domain.limitFor
import com.alain.epure.pdf.domain.resumePage
import com.alain.epure.pdf.domain.withRecent
import com.alain.epure.pdf.system.OpenError
import com.alain.epure.pdf.system.OpenResult
import com.alain.epure.pdf.system.PdfDocument
import com.alain.epure.pdf.system.readDisplayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface UiState {
    // recentUnavailable: the document opened from the list could not be reopened (DEC-0021).
    class Home(val error: OpenError? = null, val recentUnavailable: Boolean = false) : UiState

    // external: the document was opened by another app, so Back returns to that app.
    class Reader(
        val document: PdfDocument,
        val external: Boolean,
        val page: Int,
        val fraction: Float
    ) : UiState

    data object Settings : UiState
    data object About : UiState
}

// Holds the open document across rotations (SPEC F-2.6), the recent documents (SPEC F-4, F-5) and the settings.
class AppViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Home())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _recents = MutableStateFlow<List<RecentDocument>>(emptyList())
    val recents: StateFlow<List<RecentDocument>> = _recents.asStateFlow()

    private val _keepList = MutableStateFlow(false)
    val keepList: StateFlow<Boolean> = _keepList.asStateFlow()

    private val _nightMode = MutableStateFlow(false)
    val nightMode: StateFlow<Boolean> = _nightMode.asStateFlow()

    // URI of the document in the reader: its position is the one that gets saved.
    private var readerUri: String? = null

    // Positions are written after a pause in reading, not on every page change (SPEC F-5.1).
    private var saveJob: Job? = null

    init {
        container.engine.clearTemporaryCopies()
        viewModelScope.launch {
            _recents.value = withContext(Dispatchers.IO) { container.recents.load() }
        }
        viewModelScope.launch {
            container.settings.keepList.collect { _keepList.value = it }
        }
        viewModelScope.launch {
            container.settings.nightMode.collect { _nightMode.value = it }
        }
    }

    // Night mode inverts the page colours on screen (SPEC F-8.1, F-8.4).
    fun setNightMode(enabled: Boolean) {
        _nightMode.value = enabled
        viewModelScope.launch { container.settings.setNightMode(enabled) }
    }

    fun open(uri: Uri, external: Boolean = false, fromRecent: Boolean = false) {
        viewModelScope.launch {
            val name = withContext(Dispatchers.IO) { readDisplayName(container.context, uri) }
            when (val result = container.engine.open(container.context, uri)) {
                is OpenResult.Opened -> {
                    val previous = _recents.value
                    // The same file under another URI keeps its position (SPEC F-4.5, DEC-0023).
                    val sameFile = previous.firstOrNull { isSameFile(it, name) }
                    val page = resumePage(sameFile?.page ?: 0, result.document.pageCount)
                    val fraction = if (sameFile != null && page == sameFile.page) sameFile.fraction else 0f
                    val document = RecentDocument(
                        uri = uri.toString(),
                        name = name,
                        pageCount = result.document.pageCount,
                        page = page,
                        fraction = fraction
                    )
                    val updated = withRecent(previous, document, _keepList.value)

                    keepAccess(uri)
                    releaseAccessOf(before = previous, after = updated)
                    closeCurrent()
                    readerUri = document.uri
                    _recents.value = updated
                    persist()
                    _state.value = UiState.Reader(result.document, external, page, fraction)
                }
                is OpenResult.Failed -> {
                    // A document that cannot be reopened stays in the list with its position (DEC-0021).
                    closeCurrent()
                    _state.value = UiState.Home(result.error, recentUnavailable = fromRecent)
                }
            }
        }
    }

    fun openRecent(uri: String) {
        open(Uri.parse(uri), fromRecent = true)
    }

    // Called by the reader each time the reading position settles.
    fun onPosition(page: Int, fraction: Float) {
        val uri = readerUri ?: return
        _recents.value = _recents.value.map {
            if (it.uri == uri) it.copy(page = page, fraction = fraction) else it
        }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(SAVE_DELAY_MS)
            persist()
        }
    }

    // « Retirer de la liste » (SPEC F-4.4): the entry, its position and its access are removed; the file is not.
    fun removeRecent(uri: String) {
        val current = _recents.value
        val updated = current.filterNot { it.uri == uri }
        releaseAccessOf(before = current, after = updated)
        _recents.value = updated
        persist()
    }

    // Optional list (SPEC F-15.1, DEC-0024). Turning it off keeps only the last document.
    fun setKeepList(enabled: Boolean) {
        _keepList.value = enabled
        viewModelScope.launch { container.settings.setKeepList(enabled) }
        val current = _recents.value
        val updated = current.take(limitFor(enabled))
        releaseAccessOf(before = current, after = updated)
        _recents.value = updated
        persist()
    }

    fun closeReader() {
        closeCurrent()
        persist()
        _state.value = UiState.Home()
    }

    fun showHome() {
        _state.value = UiState.Home()
    }

    fun showSettings() {
        _state.value = UiState.Settings
    }

    fun showAbout() {
        _state.value = UiState.About
    }

    override fun onCleared() {
        saveJob?.cancel()
        closeCurrent()
        persist()
    }

    // Writes the current list on the process-wide scope, which outlives this ViewModel.
    private fun persist() {
        container.appScope.launch { container.recents.save(_recents.value) }
    }

    // Read access is kept so a document can be reopened later. Sources that refuse it still open for this session.
    private fun keepAccess(uri: Uri) {
        try {
            container.context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Reopening later will report the file as unavailable.
        }
    }

    // Releases the access of every entry that left the list.
    private fun releaseAccessOf(before: List<RecentDocument>, after: List<RecentDocument>) {
        val kept = after.map { it.uri }.toSet()
        before.filterNot { it.uri in kept }.forEach { releaseAccess(Uri.parse(it.uri)) }
    }

    private fun releaseAccess(uri: Uri) {
        try {
            container.context.contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // The access was never kept, or is already released.
        }
    }

    private fun closeCurrent() {
        val current = _state.value
        if (current is UiState.Reader) {
            current.document.close()
            container.pageCache.clear()
            readerUri = null
        }
    }

    private companion object {
        const val SAVE_DELAY_MS = 1_000L
    }
}
