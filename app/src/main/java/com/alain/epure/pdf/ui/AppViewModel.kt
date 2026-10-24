package com.alain.epure.pdf.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alain.epure.pdf.AppContainer
import com.alain.epure.pdf.system.OpenError
import com.alain.epure.pdf.system.OpenResult
import com.alain.epure.pdf.system.PdfDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    class Home(val error: OpenError? = null) : UiState
    // external: the document was opened by another app, so Back returns to that app.
    class Reader(val document: PdfDocument, val external: Boolean) : UiState
}

// Holds the open document across rotations (SPEC F-2.6).
class AppViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Home())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        container.engine.clearTemporaryCopies()
    }

    fun open(uri: Uri, external: Boolean = false) {
        viewModelScope.launch {
            when (val result = container.engine.open(container.context, uri)) {
                is OpenResult.Opened -> {
                    closeCurrent()
                    _state.value = UiState.Reader(result.document, external)
                }
                is OpenResult.Failed -> {
                    closeCurrent()
                    _state.value = UiState.Home(result.error)
                }
            }
        }
    }

    fun closeReader() {
        closeCurrent()
        _state.value = UiState.Home()
    }

    override fun onCleared() {
        closeCurrent()
    }

    private fun closeCurrent() {
        val current = _state.value
        if (current is UiState.Reader) {
            current.document.close()
            container.pageCache.clear()
        }
    }
}
