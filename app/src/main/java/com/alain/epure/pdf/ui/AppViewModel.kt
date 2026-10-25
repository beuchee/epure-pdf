package com.alain.epure.pdf.ui

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alain.epure.pdf.AppContainer
import com.alain.epure.pdf.domain.RecentDocument
import com.alain.epure.pdf.domain.isSameFile
import com.alain.epure.pdf.domain.resumePage
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
    // recentUnavailable: the last document could not be reopened (DEC-0021).
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

// Holds the open document across rotations (SPEC F-2.6) and the last document opened (SPEC F-4, F-5).
class AppViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Home())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _recent = MutableStateFlow<RecentDocument?>(null)
    val recent: StateFlow<RecentDocument?> = _recent.asStateFlow()

    // Positions are written after a pause in reading, not on every page change (SPEC F-5.1).
    private var saveJob: Job? = null

    init {
        container.engine.clearTemporaryCopies()
        viewModelScope.launch {
            _recent.value = withContext(Dispatchers.IO) { container.recents.load() }
        }
    }

    fun open(uri: Uri, external: Boolean = false, fromRecent: Boolean = false) {
        viewModelScope.launch {
            val name = withContext(Dispatchers.IO) { readDisplayName(container.context, uri) }
            when (val result = container.engine.open(container.context, uri)) {
                is OpenResult.Opened -> {
                    val previous = _recent.value
                    // The same file under another URI keeps its position (SPEC F-4.5, DEC-0023).
                    val sameFile = previous?.takeIf { isSameFile(it, name) }
                    val page = resumePage(sameFile?.page ?: 0, result.document.pageCount)
                    val fraction = if (sameFile != null && page == sameFile.page) sameFile.fraction else 0f

                    keepAccess(uri)
                    if (previous != null && previous.uri != uri.toString()) releaseAccess(Uri.parse(previous.uri))
                    closeCurrent()
                    _recent.value = RecentDocument(
                        uri = uri.toString(),
                        name = name,
                        pageCount = result.document.pageCount,
                        page = page,
                        fraction = fraction
                    )
                    persist()
                    _state.value = UiState.Reader(result.document, external, page, fraction)
                }
                is OpenResult.Failed -> {
                    // The last document stays in the list: a file that cannot be reopened keeps its position (DEC-0021).
                    closeCurrent()
                    _state.value = UiState.Home(result.error, recentUnavailable = fromRecent)
                }
            }
        }
    }

    fun openRecent() {
        _recent.value?.let { open(Uri.parse(it.uri), fromRecent = true) }
    }

    // Called by the reader each time the reading position settles.
    fun onPosition(page: Int, fraction: Float) {
        val current = _recent.value ?: return
        _recent.value = current.copy(page = page, fraction = fraction)
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(SAVE_DELAY_MS)
            persist()
        }
    }

    // « Retirer de la liste » (SPEC F-4.4): the entry, its position and the access are removed; the file is not.
    fun removeRecent() {
        val current = _recent.value ?: return
        saveJob?.cancel()
        _recent.value = null
        releaseAccess(Uri.parse(current.uri))
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

    // Writes the current entry (or its removal) on the process-wide scope, which outlives this ViewModel.
    private fun persist() {
        container.appScope.launch {
            val current = _recent.value
            if (current == null) container.recents.clear() else container.recents.save(current)
        }
    }

    // Read access is kept so the document can be reopened later. Sources that refuse it still open for this session.
    private fun keepAccess(uri: Uri) {
        try {
            container.context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Reopening later will report the file as unavailable.
        }
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
        }
    }

    private companion object {
        const val SAVE_DELAY_MS = 1_000L
    }
}
