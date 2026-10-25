package com.alain.epure.pdf.ui

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.alain.epure.pdf.PageKey
import com.alain.epure.pdf.R
import com.alain.epure.pdf.domain.LruByteCache
import com.alain.epure.pdf.domain.MIN_ZOOM
import com.alain.epure.pdf.domain.clampOffsetX
import com.alain.epure.pdf.domain.clampZoom
import com.alain.epure.pdf.domain.VisiblePage
import com.alain.epure.pdf.domain.currentPage
import com.alain.epure.pdf.domain.doubleTapTarget
import com.alain.epure.pdf.domain.halfPageOffsetX
import com.alain.epure.pdf.domain.offsetXForFocus
import com.alain.epure.pdf.domain.pageFraction
import com.alain.epure.pdf.domain.renderWidthPx
import com.alain.epure.pdf.domain.Tile
import com.alain.epure.pdf.domain.pageHeightPx
import com.alain.epure.pdf.domain.tilesFor
import com.alain.epure.pdf.domain.scrollOffsetForFocus
import com.alain.epure.pdf.system.PageRect
import com.alain.epure.pdf.system.PageSize
import com.alain.epure.pdf.system.PdfDocument
import com.alain.epure.pdf.system.shareDocument
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Continuous vertical reader with zoom (SPEC F-2 and F-3).
@Composable
fun ReaderScreen(
    document: PdfDocument,
    cache: LruByteCache<PageKey, Bitmap>,
    initialPage: Int,
    initialFraction: Float,
    onPosition: (page: Int, fraction: Float) -> Unit,
    nightMode: Boolean,
    onToggleNightMode: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    documentName: String,
    documentUri: String,
    bookmarks: List<Int>,
    onToggleBookmark: (page: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // Commands: a tap shows or hides them. They hide by themselves after a pause, or when the page scrolls (SPEC F-6.1, F-6.2).
    var commandsVisible by remember { mutableStateOf(false) }
    var interactions by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    var goToOpen by remember { mutableStateOf(false) }
    var bookmarksOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    val search = remember(document) { ReaderSearch(document) }
    val dialogOpen = menuOpen || goToOpen || bookmarksOpen || searchOpen

    ImmersiveMode(showBars = commandsVisible)
    KeepScreenOn(keepScreenOn)
    BackHandler(onBack = onBack)
    // Registered after the handler above, so it takes precedence while it is enabled:
    // the open window closes first, then the commands, then the reader (SPEC F-6.4).
    // An open search ends last, after the commands (SPEC F-11.5).
    BackHandler(enabled = dialogOpen || commandsVisible || search.active) {
        when {
            menuOpen -> menuOpen = false
            goToOpen -> goToOpen = false
            bookmarksOpen -> bookmarksOpen = false
            searchOpen -> searchOpen = false
            commandsVisible -> commandsVisible = false
            else -> search.close()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Black)) {
        val viewportWidthDp = maxWidth
        val viewportWidthPx = with(LocalDensity.current) { viewportWidthDp.roundToPx() }.toFloat()
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        // The commands leave after a pause without interaction; the pause restarts on each tap (SPEC F-6.2).
        LaunchedEffect(commandsVisible, interactions, dialogOpen) {
            if (commandsVisible && !dialogOpen) {
                delay(COMMANDS_HIDE_DELAY_MS)
                commandsVisible = false
            }
        }
        // Scrolling hides the commands at once (SPEC F-6.2).
        LaunchedEffect(listState.isScrollInProgress) {
            if (listState.isScrollInProgress) commandsVisible = false
        }
        // Each occurrence is brought to the top of its page (SPEC F-11.3).
        LaunchedEffect(search.status, search.current) {
            search.currentPage?.let { listState.scrollToItem(it) }
        }
        // The page shown in the indicator is the one taking most of the screen (SPEC F-7.1).
        val currentPageIndex by remember { derivedStateOf { currentPage(visiblePages(listState)) } }

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
        // Starts at the saved position when the document is opened again (SPEC F-5.2).
        var anchorPage by rememberSaveable { mutableIntStateOf(initialPage) }
        var anchorFraction by rememberSaveable { mutableFloatStateOf(initialFraction) }

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
                    if (item != null && item.size > 0) anchorFraction = pageFraction(offset, item.size)
                    onPosition(anchorPage, anchorFraction)
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
                        onTap = {
                            commandsVisible = !commandsVisible
                            interactions++
                        },
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
                    ReaderPage(
                        document,
                        index,
                        viewportWidthPx.roundToInt(),
                        renderScale,
                        scale,
                        offsetX,
                        listState,
                        cache,
                        nightMode,
                        search.highlightsOn(index)
                    )
                }
            }
        }

        // The page the reader shows: the one taking most of the screen, or the saved one before the first layout.
        val shownPage = currentPageIndex ?: anchorPage
        if (commandsVisible) {
            CommandsTop(name = documentName, modifier = Modifier.align(Alignment.TopStart))
        }
        // While a search is open, its bar takes the place of the page indicator and « Plus » (SPEC F-11.3).
        if (search.active) {
            SearchBar(search, modifier = Modifier.align(Alignment.BottomStart))
        } else if (commandsVisible) {
            CommandsBottom(
                page = shownPage + 1,
                pageCount = document.pageCount,
                onPageClick = { goToOpen = true },
                onMoreClick = { menuOpen = true },
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
        if (menuOpen) {
            MoreMenu(
                bookmarked = shownPage in bookmarks,
                hasBookmarks = bookmarks.isNotEmpty(),
                // The menu stays open, so the grey action shows its new state at once.
                onToggleBookmark = { onToggleBookmark(shownPage) },
                onShowBookmarks = {
                    menuOpen = false
                    bookmarksOpen = true
                },
                onSearch = {
                    menuOpen = false
                    searchOpen = true
                },
                nightMode = nightMode,
                onToggleNightMode = onToggleNightMode,
                keepScreenOn = keepScreenOn,
                onToggleKeepScreenOn = onToggleKeepScreenOn,
                onShare = { shareDocument(context, Uri.parse(documentUri), documentName) },
                onDismiss = { menuOpen = false }
            )
        }
        if (goToOpen) {
            GoToPageDialog(
                pageCount = document.pageCount,
                onDismiss = { goToOpen = false },
                onGo = { page ->
                    goToOpen = false
                    commandsVisible = false
                    scope.launch { listState.scrollToItem(page - 1) }
                }
            )
        }
        if (bookmarksOpen) {
            BookmarksDialog(
                pages = bookmarks,
                onGo = { page ->
                    bookmarksOpen = false
                    commandsVisible = false
                    scope.launch { listState.scrollToItem(page) }
                },
                onRemove = onToggleBookmark,
                onDismiss = { bookmarksOpen = false }
            )
        }
        if (searchOpen) {
            SearchDialog(
                searchable = document.searchable,
                onSearch = { query ->
                    searchOpen = false
                    search.start(scope, query)
                },
                onDismiss = { searchOpen = false }
            )
        }
    }
}

// Pages on screen with the height each one takes (SPEC F-7.1).
private fun visiblePages(state: LazyListState): List<VisiblePage> {
    val layout = state.layoutInfo
    return layout.visibleItemsInfo.map { item ->
        val top = maxOf(item.offset, layout.viewportStartOffset)
        val bottom = minOf(item.offset + item.size, layout.viewportEndOffset)
        VisiblePage(item.index, bottom - top)
    }
}

@Composable
private fun ReaderPage(
    document: PdfDocument,
    index: Int,
    viewportWidthPx: Int,
    renderScale: Float,
    scale: Float,
    offsetX: Float,
    listState: LazyListState,
    cache: LruByteCache<PageKey, Bitmap>,
    nightMode: Boolean,
    highlights: List<PageRect>
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

    // When the zoom wants more pixels than the whole-page budget allows, the visible part of the page
    // is also drawn in sharp tiles over the whole page (SPEC F-3.3, DEC-0040). Null when no tiles are needed.
    val wantedWidth = (viewportWidthPx * renderScale).roundToInt()
    val tileSpace = renderWidth?.takeIf { it < wantedWidth }?.let { width ->
        pageSize?.let { width to pageHeightPx(width, it.width, it.height) }
    }
    // Pages are laid out at the live zoom, the tiles at the rendered zoom: the ratio converts between them.
    val live = scale / renderScale
    val tiles = tileSpace?.let { (width, height) ->
        val band = pageBandOnScreen(listState, index) ?: return@let emptyList()
        tilesFor(
            pageWidthPx = width,
            pageHeightPx = height,
            visibleLeft = (-offsetX / live).roundToInt(),
            visibleTop = (band.first / live).roundToInt(),
            visibleRight = ((viewportWidthPx - offsetX) / live).roundToInt(),
            visibleBottom = (band.second / live).roundToInt()
        )
    }.orEmpty()
    val tileBitmaps = remember(document, index, renderWidth) { mutableStateMapOf<Tile, Bitmap>() }

    LaunchedEffect(document, index, tileSpace, tiles) {
        val (width, height) = tileSpace ?: return@LaunchedEffect
        tileBitmaps.keys.filter { it !in tiles }.forEach { tileBitmaps.remove(it) }
        for (tile in tiles) {
            if (tile in tileBitmaps) continue
            val key = PageKey(index, width, tile)
            val bitmap = cache.get(key) ?: document.renderTile(index, width, height, tile)?.also { cache.put(key, it) }
            if (bitmap != null) tileBitmaps[tile] = bitmap
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
                modifier = Modifier.fillMaxSize(),
                colorFilter = if (nightMode) NightFilter else null
            )
            failed -> Text(
                text = stringResource(R.string.page_unreadable),
                style = LabelStyle,
                color = TextSecondary
            )
            else -> Box(Modifier.fillMaxSize().background(if (nightMode) Color.Black else Color.White))
        }
        if (tileSpace != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Tiles are placed at the live size of the page, like the whole page above them.
                val factor = this.size.width / tileSpace.first
                tileBitmaps.forEach { (tile, tileBitmap) ->
                    drawImage(
                        image = tileBitmap.asImageBitmap(),
                        dstOffset = IntOffset((tile.left * factor).roundToInt(), (tile.top * factor).roundToInt()),
                        dstSize = IntSize((tile.width * factor).roundToInt(), (tile.height * factor).roundToInt()),
                        colorFilter = if (nightMode) NightFilter else null
                    )
                }
            }
        }
        // Every occurrence of the search is drawn as its negative: the colours under it are inverted,
        // so black on white becomes white on black, and the reverse in night mode (SPEC F-11.4).
        if (highlights.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = this.size.width
                val height = this.size.height
                highlights.forEach { rect ->
                    drawRect(
                        color = TextPrimary,
                        blendMode = BlendMode.Difference,
                        topLeft = Offset(rect.left * width, rect.top * height),
                        size = Size((rect.right - rect.left) * width, (rect.bottom - rect.top) * height)
                    )
                }
            }
        }
    }
}

// Top and bottom of the screen in pixels from the top of a page, at the live zoom (null when the page is not shown).
private fun pageBandOnScreen(state: LazyListState, index: Int): Pair<Float, Float>? {
    val layout = state.layoutInfo
    val item = layout.visibleItemsInfo.firstOrNull { it.index == index } ?: return null
    return (layout.viewportStartOffset - item.offset).toFloat() to (layout.viewportEndOffset - item.offset).toFloat()
}

// The screen stays on while the reader is shown and the setting is on (SPEC F-9.1).
// Leaving the reader restores the normal timeout.
@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

// Full-screen reading: hide the system bars, they come back on a swipe from the edge (SPEC F-2.4).
// The system bars come back with the commands, and leave with them (CHARTE B.4).
@Composable
private fun ImmersiveMode(showBars: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, showBars) {
        val window = (view.context as? Activity)?.window
        val controller = window?.insetsController
        controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (showBars) {
            controller?.show(WindowInsets.Type.systemBars())
        } else {
            controller?.hide(WindowInsets.Type.systemBars())
        }
        onDispose { controller?.show(WindowInsets.Type.systemBars()) }
    }
}

// Night mode (SPEC F-8.1, F-8.4): inverts each colour, so white becomes black and black text becomes white.
// It is a display filter: pages are not rendered again. Images are inverted too (SPEC F-8.2).
private val NightFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

private const val A4_RATIO = 595f / 842f

// Pause without interaction after which the commands hide (SPEC F-6.2, decided at 4 s).
private const val COMMANDS_HIDE_DELAY_MS = 4_000L
private const val RENDER_SETTLE_MS = 250L
