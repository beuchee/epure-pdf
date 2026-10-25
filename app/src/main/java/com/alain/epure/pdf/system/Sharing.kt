package com.alain.epure.pdf.system

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.IOException

// Sends a document to another app with a temporary read right (SPEC F-12.1).
// Returns false when the document cannot be read here or no app can receive it (SPEC F-12.2).
fun shareDocument(context: Context, uri: Uri, name: String): Boolean {
    val resolver = context.contentResolver
    return try {
        resolver.openFileDescriptor(uri, "r")?.close() ?: return false
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(resolver, name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, null))
        true
    } catch (e: IOException) {
        false
    } catch (e: SecurityException) {
        false
    } catch (e: ActivityNotFoundException) {
        false
    }
}
