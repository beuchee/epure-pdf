package com.alain.epure.pdf.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.dp
import com.alain.epure.pdf.BuildConfig
import com.alain.epure.pdf.R
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.Black
import com.alain.epure.pdf.ui.theme.LabelStyle
import com.alain.epure.pdf.ui.theme.TextPrimary
import com.alain.epure.pdf.ui.theme.TextSecondary

// « À propos » (SPEC F-15.2): name, version, licence, repository, privacy and the rendering engine.
@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var noBrowser by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.about),
            style = AppNameStyle,
            color = TextPrimary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = LabelStyle,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            style = LabelStyle,
            color = TextSecondary
        )
        Text(
            text = stringResource(R.string.about_license),
            style = LabelStyle,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        ActionRow(text = stringResource(R.string.about_repository)) {
            noBrowser = !openRepository(context)
        }
        if (noBrowser) {
            Text(
                text = stringResource(R.string.error_no_browser),
                style = LabelStyle,
                color = TextSecondary
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.about_privacy_title),
            style = LabelStyle,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.about_privacy_text),
            style = LabelStyle,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.about_engine_title),
            style = LabelStyle,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.about_engine_text),
            style = LabelStyle,
            color = TextSecondary
        )
    }
}

// Opens the repository in the browser. The app itself needs no network permission (DEC-0005).
private fun openRepository(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY_URL))
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

private const val REPOSITORY_URL = "https://github.com/beuchee/epure-pdf"
