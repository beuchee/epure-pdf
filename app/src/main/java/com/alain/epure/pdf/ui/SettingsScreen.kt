package com.alain.epure.pdf.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.pdf.R
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.NoteStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary

// Settings list (CHARTE A.6, "Liste de réglages"): a title, then actions in a list.
@Composable
fun SettingsScreen(
    keepList: Boolean,
    onToggleKeepList: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    nightMode: Boolean,
    onToggleNightMode: (Boolean) -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = AppNameStyle,
            color = TextPrimary
        )
        Spacer(Modifier.height(24.dp))
        // A setting shows its state under its name; a tap switches it (SPEC F-15.1).
        ActionRow(text = stringResource(R.string.keep_list)) { onToggleKeepList(!keepList) }
        Text(
            text = stringResource(if (keepList) R.string.state_on else R.string.state_off),
            style = LabelStyle,
            color = TextSecondary
        )
        Text(
            text = stringResource(R.string.keep_list_help),
            style = NoteStyle,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        ActionRow(text = stringResource(R.string.keep_screen_on)) { onToggleKeepScreenOn(!keepScreenOn) }
        Text(
            text = stringResource(if (keepScreenOn) R.string.state_on else R.string.state_off),
            style = LabelStyle,
            color = TextSecondary
        )
        Text(
            text = stringResource(R.string.keep_screen_on_help),
            style = NoteStyle,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        ActionRow(text = stringResource(R.string.night_mode)) { onToggleNightMode(!nightMode) }
        Text(
            text = stringResource(if (nightMode) R.string.state_on else R.string.state_off),
            style = LabelStyle,
            color = TextSecondary
        )
        Text(
            text = stringResource(R.string.night_mode_help),
            style = NoteStyle,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        ActionRow(text = stringResource(R.string.about), onClick = onAbout)
    }
}
