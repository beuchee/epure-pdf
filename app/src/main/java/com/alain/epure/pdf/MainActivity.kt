package com.alain.epure.pdf

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.alain.epure.pdf.ui.AppViewModel
import com.alain.epure.pdf.ui.HomeScreen
import com.alain.epure.pdf.ui.ReaderScreen
import com.alain.epure.pdf.ui.UiState
import com.alain.epure.pdf.ui.theme.EpureTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    private val viewModel: AppViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                AppViewModel(container) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always dark, whatever the system theme: light system icons on black.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        setContent {
            EpureTheme {
                val state by viewModel.state.collectAsState()
                when (val current = state) {
                    is UiState.Home -> HomeScreen(error = current.error, onOpen = viewModel::open)
                    is UiState.Reader -> ReaderScreen(
                        document = current.document,
                        cache = container.pageCache,
                        onBack = viewModel::closeReader
                    )
                }
            }
        }
    }
}
