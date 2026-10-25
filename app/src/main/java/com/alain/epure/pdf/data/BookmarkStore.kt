package com.alain.epure.pdf.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private val Context.bookmarksDataStore by preferencesDataStore(name = "bookmarks")

// Bookmarks by document URI, in the app's private storage (SPEC F-10.4, DEC-0035).
// Only page numbers are kept; the document content is never stored (SPEC T-2).
class BookmarkStore(private val context: Context) {
    // A damaged entry is dropped rather than blocking the reader.
    suspend fun load(): Map<String, List<Int>> = try {
        context.bookmarksDataStore.data.first()[BOOKMARKS]?.let { fromJson(it) }.orEmpty()
    } catch (e: IOException) {
        emptyMap()
    } catch (e: JSONException) {
        emptyMap()
    }

    suspend fun save(bookmarks: Map<String, List<Int>>) {
        context.bookmarksDataStore.edit { it[BOOKMARKS] = toJson(bookmarks) }
    }

    private fun toJson(bookmarks: Map<String, List<Int>>): String {
        val json = JSONObject()
        bookmarks.forEach { (uri, pages) -> json.put(uri, JSONArray(pages)) }
        return json.toString()
    }

    private fun fromJson(text: String): Map<String, List<Int>> {
        val json = JSONObject(text)
        return json.keys().asSequence().associateWith { uri ->
            val array = json.getJSONArray(uri)
            List(array.length()) { array.getInt(it) }
        }
    }

    private companion object {
        val BOOKMARKS = stringPreferencesKey("bookmarks")
    }
}
