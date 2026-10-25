package com.alain.epure.pdf.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alain.epure.pdf.domain.RecentDocument
import java.io.IOException
import kotlinx.coroutines.flow.first
import org.json.JSONException
import org.json.JSONObject

private val Context.recentsDataStore by preferencesDataStore(name = "recents")

// Keeps the last document and its reading position, in the app's private storage (DEC-0024, DEC-0027).
// The document content is never stored, only its URI and position.
class RecentStore(private val context: Context) {
    suspend fun load(): RecentDocument? = try {
        context.recentsDataStore.data.first()[LAST_DOCUMENT]?.let(::fromJson)
    } catch (e: IOException) {
        null
    } catch (e: JSONException) {
        // A damaged entry is dropped rather than blocking the home screen.
        null
    }

    suspend fun save(document: RecentDocument) {
        context.recentsDataStore.edit { it[LAST_DOCUMENT] = toJson(document) }
    }

    suspend fun clear() {
        context.recentsDataStore.edit { it.remove(LAST_DOCUMENT) }
    }

    private fun toJson(document: RecentDocument): String = JSONObject()
        .put(KEY_URI, document.uri)
        .put(KEY_NAME, document.name)
        .put(KEY_PAGE_COUNT, document.pageCount)
        .put(KEY_PAGE, document.page)
        .put(KEY_FRACTION, document.fraction.toDouble())
        .toString()

    private fun fromJson(text: String): RecentDocument {
        val json = JSONObject(text)
        return RecentDocument(
            uri = json.getString(KEY_URI),
            name = json.getString(KEY_NAME),
            pageCount = json.getInt(KEY_PAGE_COUNT),
            page = json.getInt(KEY_PAGE),
            fraction = json.getDouble(KEY_FRACTION).toFloat()
        )
    }

    private companion object {
        val LAST_DOCUMENT = stringPreferencesKey("last_document")
        const val KEY_URI = "uri"
        const val KEY_NAME = "name"
        const val KEY_PAGE_COUNT = "pageCount"
        const val KEY_PAGE = "page"
        const val KEY_FRACTION = "fraction"
    }
}
