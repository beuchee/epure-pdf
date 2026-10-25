package com.alain.epure.pdf.ui

import android.app.Activity
import android.graphics.Bitmap
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.pdf.PageKey
import com.alain.epure.pdf.R
import com.alain.epure.pdf.domain.LruByteCache
import com.alain.epure.pdf.domain.MIN_ZOOM
import com.alain.epure.pdf.domain.clampOffsetX
import com.alain.epure.pdf.domain.clampZoom
import com.alain.epure.pdf.domain.doubleTapTarget
import com.alain.epure.pdf.domain.halfPageOffsetX
import com.alain.epure.pdf.domain.offsetXForFocus
import com.alain.epure.pdf.domain.renderWidthPx
import com.alain.epure.pdf.domain.scrollOffsetForFocus
import com.alain.epure.pdf.system.PageSize
import com.alain.epure.pdf.system.PdfDocument
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.TextSecondary
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Continuous vertical reader with zoom (SPEC F-2 and F-3).
@Composable
fun ReaderScreen(
    document: PdfDocument,
    cache: LruByteCache<PageKey, Bitmap>,
    onBack: () -> Unit
) {
    ImmersiveMode()
    BackHandler(onBack = onBack)

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Black)) {
        val viewportWidthDp = maxWidth
        val viewportWidthPx = with(LocalDensity.current) { viewportWidthDp.roundToPx() }.toFloat()
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        // scale follows the fingers at once; renderScale only changes once the zoom has settled.
        var scale by remember { mutableFloatStateOf(MIN_ZOOM) }
        var renderScale by remember { mutableFloatStateOf(MIN_ZOOM) }
        var offsetX by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(scale) {
            delay(RENDER_SETTLE_MS)
            renderScale = scale
        }

        // The reading position survives a rotation as page + fraction of that page, because
        // page heights change with the width (SPEC F-2.6).
        var anchorPage by rememberSaveable { mutableIntStateOf(0) }
        var anchorFraction by rememberSaveable { mutableFloatStateOf(0f) }

        // Restore first, then track: tracking during the restore would overwrite the fraction.
        LaunchedEffect(document) {
            if (anchorPage != 0 || anchorFraction != 0f) {
                val size = document.pageSize(anchorPage)
                val heightPx = viewportWidthPx * size.height / size.width
                listState.scrollToItem(anchorPage, (anchorFraction * heightPx).roundToInt())
            }
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    anchorPage = index
                    val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    if (item != null && item.size > 0) anchorFraction = offset.toFloat() / item.size
                }
        }

        // Keeps the point under the finger still under it when the zoom changes (SPEC F-3.2).
        fun keepFocusStill(focusY: Float, factor: Float) {
            val item = listState.layoutInfo.visibleItemsInfo.firstOrNull {
                focusY >= it.offset && focusY < it.offset + it.size
            } ?: return
            scope.launch {
                listState.scrollToItem(item.index, scrollOffsetForFocus(focusY, item.offset.toFloat(), factor))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { position: Offset ->
                            val target = doubleTapTarget(scale)
                            keepFocusStill(position.y, target / scale)
                            offsetX = if (target == MIN_ZOOM) {
                                0f
                            } else {
                                halfPageOffsetX(position.x, viewportWidthPx, target)
                            }
                            scale = target
                            renderScale = target
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val oldScale = scale
                        val newScale = clampZoom(oldScale * zoom)
                        offsetX = clampOffsetX(
                            offsetXForFocus(offsetX, centroid.x, oldScale, newScale) + pan.x,
                            newScale,
                            viewportWidthPx
                        )
                        if (newScale != oldScale) keepFocusStill(centroid.y, newScale / oldScale)
                        scale = newScale
                    }
                }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.layout { measurable, constraints ->
                    // Measured at the zoomed width and placed exactly at offsetX, never centred.
                    val widthPx = (viewportWidthPx * scale).roundToInt()
                    val placeable = measurable.measure(
                        constraints.copy(minWidth = widthPx, maxWidth = widthPx)
                    )
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.place(offsetX.roundToInt(), 0)
                    }
                },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(document.pageCount) { index ->
                    ReaderPage(document, index, viewportWidthPx.roundToInt(), renderScale, cache)
                }
            }
        }
    }
}

@Composable
private fun ReaderPage(
    document: PdfDocument,
    index: Int,
    viewportWidthPx: Int,
    renderScale: Float,
    cache: LruByteCache<PageKey, Bitmap>
) {
    val size by produceState<PageSize?>(null, document, index) {
        value = document.pageSize(index)
    }
    val pageSize = size
    val label = pluralStringResource(R.plurals.page_description, document.pageCount, index + 1, document.pageCount)

    // Until the size is known, a standard A4 proportion keeps the layout stable.
    val ratio = pageSize?.let { it.width / it.height } ?: A4_RATIO

    // The last bitmap stays on screen while a sharper one is rendered (SPEC F-3.3).
    var shown by remember(document, index) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(document, index) { mutableStateOf(false) }
    val renderWidth = pageSize?.let {
        renderWidthPx(viewportWidthPx, renderScale, it.height / it.width)
    }

    LaunchedEffect(document, index, renderWidth) {
        if (renderWidth == null) return@LaunchedEffect
        val key = PageKey(index, renderWidth)
        val cached = cache.get(key)
        if (cached != null) {
            shown = cached
            failed = false
            return@LaunchedEffect
        }
        val bitmap = document.render(index, renderWidth)
        if (bitmap != null) {
            cache.put(key, bitmap)
            shown = bitmap
            failed = false
        } else {
            failed = shown == null
        }
    }

    val bitmap = shown
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(ratio),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize()
            )
            failed -> Text(
                text = stringResource(R.string.page_unreadable),
                style = LabelStyle,
                color = TextSecondary
            )
            else -> Box(Modifier.fillMaxSize().background(Color.White))
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
private const val RENDER_SETTLE_MS = 250L
