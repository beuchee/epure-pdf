package com.alain.epure.pdf.system

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

// The display name a provider gives a document. Never throws: when the provider refuses
// the query, the last path segment of the URI is used instead (SPEC F-1.5).
fun readDisplayName(context: Context, uri: Uri): String {
    val fallbackName = uri.lastPathSegment ?: uri.toString()
    return try {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                cursor.getString(nameIndex)
            } else {
                fallbackName
            }
        } ?: fallbackName
    } catch (e: SecurityException) {
        fallbackName
    } catch (e: IllegalArgumentException) {
        fallbackName
    }
}
