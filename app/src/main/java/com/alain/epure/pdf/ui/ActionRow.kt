package com.alain.epure.pdf.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alain.epure.pdf.ui.theme.AppNameStyle
import com.alain.epure.pdf.ui.theme.TextPrimary

// Text-only action of a list (CHARTE A.6, "Action en liste").
@Composable
fun ActionRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = AppNameStyle,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
    )
}
