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
    class Reader(val document: PdfDocument) : UiState
}

// Holds the open document across rotations (SPEC F-2.6).
class AppViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Home())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun open(uri: Uri) {
        viewModelScope.launch {
            when (val result = container.engine.open(container.context, uri)) {
                is OpenResult.Opened -> {
                    closeCurrent()
                    _state.value = UiState.Reader(result.document)
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
