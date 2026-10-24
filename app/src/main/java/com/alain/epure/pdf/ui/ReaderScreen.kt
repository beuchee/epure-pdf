package com.alain.epure.pdf.ui

import android.app.Activity
import android.graphics.Bitmap
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.pdf.AppContainer
import com.alain.epure.pdf.PageKey
import com.alain.epure.pdf.R
import com.alain.epure.pdf.domain.LruByteCache
import com.alain.epure.pdf.system.PageSize
import com.alain.epure.pdf.system.PdfDocument
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.TextSecondary

// Continuous vertical reader (SPEC F-2.1 to F-2.5).
@Composable
fun ReaderScreen(
    document: PdfDocument,
    cache: LruByteCache<PageKey, Bitmap>,
    onBack: () -> Unit
) {
    ImmersiveMode()
    BackHandler(onBack = onBack)

    // Pages fit the screen width, with no side margin (SPEC F-2.2).
    val density = LocalDensity.current
    val widthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Black),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(document.pageCount) { index ->
            ReaderPage(document, index, widthPx, cache)
        }
    }
}

private sealed interface RenderState {
    data object Loading : RenderState
    class Ready(val bitmap: Bitmap) : RenderState
    data object Failed : RenderState
}

@Composable
private fun ReaderPage(
    document: PdfDocument,
    index: Int,
    widthPx: Int,
    cache: LruByteCache<PageKey, Bitmap>
) {
    val size by produceState<PageSize?>(null, document, index) {
        value = document.pageSize(index)
    }
    val pageSize = size
    val label = pluralStringResource(R.plurals.page_description, document.pageCount, index + 1, document.pageCount)

    // Until the size is known, a standard A4 proportion keeps the layout stable.
    val ratio = pageSize?.let { it.width / it.height } ?: A4_RATIO

    val render by produceState<RenderState>(RenderState.Loading, document, index, widthPx, pageSize) {
        if (pageSize == null) return@produceState
        val key = PageKey(index, widthPx)
        val cached = cache.get(key)
        if (cached != null) {
            value = RenderState.Ready(cached)
            return@produceState
        }
        val bitmap = document.render(index, widthPx)
        value = if (bitmap != null) {
            cache.put(key, bitmap)
            RenderState.Ready(bitmap)
        } else {
            RenderState.Failed
        }
    }

    val state = render
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(ratio),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            RenderState.Loading -> Box(Modifier.fillMaxSize().background(Color.White))
            is RenderState.Ready -> Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize()
            )
            RenderState.Failed -> Text(
                text = stringResource(R.string.page_unreadable),
                style = LabelStyle,
                color = TextSecondary
            )
        }
    }
}

// Full-screen reading: hide the system bars, they come back on a swipe from the edge (SPEC F-2.4).
@Composable
private fun ImmersiveMode() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.insetsController
        controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsets.Type.systemBars())
        onDispose { controller?.show(WindowInsets.Type.systemBars()) }
    }
}

private const val A4_RATIO = 595f / 842f
