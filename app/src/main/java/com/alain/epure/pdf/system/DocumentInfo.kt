package com.alain.epure.pdf.system

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

// Name and size as the provider reports them. Size is -1 when the provider does not know it.
data class DocumentInfo(val name: String, val sizeBytes: Long)

// Reads the display name and size of a document. Never throws: a provider that refuses
// the query gives a fallback name and an unknown size (SPEC F-1.5).
fun readDocumentInfo(context: Context, uri: Uri): DocumentInfo {
    val fallbackName = uri.lastPathSegment ?: uri.toString()
    return try {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                DocumentInfo(
                    name = if (nameIndex >= 0 && !cursor.isNull(nameIndex)) cursor.getString(nameIndex) else fallbackName,
                    sizeBytes = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else -1L
                )
            } else {
                DocumentInfo(fallbackName, -1L)
            }
        } ?: DocumentInfo(fallbackName, -1L)
    } catch (e: SecurityException) {
        DocumentInfo(fallbackName, -1L)
    } catch (e: IllegalArgumentException) {
        DocumentInfo(fallbackName, -1L)
    }
}
