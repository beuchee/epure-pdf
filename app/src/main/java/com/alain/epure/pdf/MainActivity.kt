package com.alain.epure.pdf

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.IntentCompat
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
                val recent by viewModel.recent.collectAsState()
                when (val current = state) {
                    is UiState.Home -> HomeScreen(
                        error = current.error,
                        recentUnavailable = current.recentUnavailable,
                        recent = recent,
                        onOpen = { viewModel.open(it) },
                        onOpenRecent = { viewModel.openRecent() },
                        onRemoveRecent = { viewModel.removeRecent() }
                    )
                    is UiState.Reader -> ReaderScreen(
                        document = current.document,
                        cache = container.pageCache,
                        initialPage = current.page,
                        initialFraction = current.fraction,
                        onPosition = { page, fraction -> viewModel.onPosition(page, fraction) },
                        onBack = {
                            // A document opened by another app: Back returns to that app (DEC-0020).
                            if (current.external) finish() else viewModel.closeReader()
                        }
                    )
                }
            }
        }
        if (savedInstanceState == null) {
            externalUri(intent)?.let { viewModel.open(it, external = true) }
        }
    }

    // Documents sent by other apps: "Ouvrir avec" (VIEW) or "Partager" (SEND), SPEC F-1.2.
    private fun externalUri(intent: Intent?): Uri? = when (intent?.action) {
        Intent.ACTION_VIEW -> intent.data
        Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        else -> null
    }
}
