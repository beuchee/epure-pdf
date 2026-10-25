package com.alain.epure.pdf.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alain.epure.pdf.domain.RecentDocument
import java.io.IOException
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private val Context.recentsDataStore by preferencesDataStore(name = "recents")

// Keeps the recent documents and their reading positions, in the app's private storage (DEC-0024, DEC-0027).
// The document content is never stored, only its URI and position.
class RecentStore(private val context: Context) {
    // Most recent first. A damaged entry is dropped rather than blocking the home screen.
    suspend fun load(): List<RecentDocument> = try {
        val preferences = context.recentsDataStore.data.first()
        val list = preferences[RECENT_LIST]
        if (list != null) {
            fromJsonArray(list)
        } else {
            // Earlier versions kept only the last document under another key.
            preferences[LEGACY_LAST_DOCUMENT]?.let { listOf(fromJsonObject(JSONObject(it))) }.orEmpty()
        }
    } catch (e: IOException) {
        emptyList()
    } catch (e: JSONException) {
        emptyList()
    }

    suspend fun save(recents: List<RecentDocument>) {
        context.recentsDataStore.edit { it[RECENT_LIST] = toJsonArray(recents) }
    }

    private fun toJsonArray(recents: List<RecentDocument>): String =
        JSONArray().apply { recents.forEach { put(toJsonObject(it)) } }.toString()

    private fun fromJsonArray(text: String): List<RecentDocument> {
        val array = JSONArray(text)
        return List(array.length()) { fromJsonObject(array.getJSONObject(it)) }
    }

    private fun toJsonObject(document: RecentDocument): JSONObject = JSONObject()
        .put(KEY_URI, document.uri)
        .put(KEY_NAME, document.name)
        .put(KEY_PAGE_COUNT, document.pageCount)
        .put(KEY_PAGE, document.page)
        .put(KEY_FRACTION, document.fraction.toDouble())

    private fun fromJsonObject(json: JSONObject): RecentDocument = RecentDocument(
        uri = json.getString(KEY_URI),
        name = json.getString(KEY_NAME),
        pageCount = json.getInt(KEY_PAGE_COUNT),
        page = json.getInt(KEY_PAGE),
        fraction = json.getDouble(KEY_FRACTION).toFloat()
    )

    private companion object {
        val RECENT_LIST = stringPreferencesKey("recent_documents")
        val LEGACY_LAST_DOCUMENT = stringPreferencesKey("last_document")
        const val KEY_URI = "uri"
        const val KEY_NAME = "name"
        const val KEY_PAGE_COUNT = "pageCount"
        const val KEY_PAGE = "page"
        const val KEY_FRACTION = "fraction"
    }
}
