package com.uet.nvmnghia.yacv.model.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.uet.nvmnghia.yacv.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


/**
 * Interface for all searchable metadata:
 * Author, Character, Comic, Folder, Genre, Series
 */
interface SearchableMetadata {
    fun getID(): Long

    fun getLabel(): String
}

/**
 * Interface for all searchable metadata DAO.
 */
interface SearchableMetadataDao<T : SearchableMetadata> {
    fun search(name: String, limit: Int = Int.MAX_VALUE): List<T>    // Covariant shit
}


/**
 * Number of preview match.
 */
const val NUM_PREVIEW_MATCH = 3


suspend fun AppDatabase.search(term: String, preview: Boolean): LiveData<List<List<SearchableMetadata>>> = liveData {
    val searchableMetadataDaos = listOf(authorDao(), characterDao(),
        comicDao(), genreDao(), seriesDao())
    val limit = if (preview) NUM_PREVIEW_MATCH + 1    // Preview 3 matches, the 4th one is used to check
        else Int.MAX_VALUE                            // if there's more
    val lists = mutableListOf<List<SearchableMetadata>>()

    searchableMetadataDaos.map { dao ->
        withContext(Dispatchers.IO) {
            val list = dao.search(term, limit)
            delay(1000)    // TODO: delete after checking that this works

            synchronized(lists) {
                lists.add(list)
            }
            emit(lists)
        }
    }
}