package com.uet.nvmnghia.yacv.model.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.uet.nvmnghia.yacv.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


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
    // TODO: convert to suspend and check if it is cancellable
    fun search(name: String, limit: Int = Int.MAX_VALUE): List<T>    // Covariant shit
}


/**
 * This class is very similar to a repository.
 * Is it a good design then?
 */
@Singleton
class MetadataSearchHandler @Inject constructor(
    private val appDb: AppDatabase
) {

    /**
     * Given a [term], search the whole database for it.
     * Searchable tables are given above.
     * If [preview] is set, only returns the first [NUM_PREVIEW_MATCH] + 1 results.
     */
    suspend fun search(term: String, preview: Boolean = false):
            LiveData<List<List<SearchableMetadata>>> = liveData(timeoutInMs = 1200) {

        // Some advantages over manual coroutine + channel:
        // - Incremental update of result list
        // - Automatic cancellation: instead of passing back & forth
        //   scope/job to call cancel(), just unsubscribe, or wait until timeout
        // - Returns List<List<SearchableMetadata>>: instead of returning
        //   both the job and the channel created, just returns a LiveData wrapper
        //
        // Note that this is a bit overengineered, but the aim is to understand coroutine
        // and find a better way than passing a callback like the below example:
        // https://play.kotlinlang.org/hands-on/Introduction%20to%20Coroutines%20and%20Channels/08_Channels
        // dao.search() is likely to NOT cooperate anyway

        val searchableMetadataDaos = appDb.run { listOf(
            authorDao(), characterDao(), comicDao(), genreDao(), seriesDao()) }
        val limit = if (preview) NUM_PREVIEW_MATCH + 1    // Preview 3 matches, the 4th one is used to check
            else Int.MAX_VALUE                            // if there's more
        val lists = mutableListOf<List<SearchableMetadata>>()

        searchableMetadataDaos.map { dao ->
            withContext(Dispatchers.IO) {
                val list = dao.search(term, limit)
                delay((Random.nextFloat() * 1000).toLong())    // TODO: delete after checking that this works

                synchronized(lists) {
                    lists.add(list)
                }
                emit(lists)
            }
        }
    }

    companion object {
        /**
         * Number of preview match.
         */
        const val NUM_PREVIEW_MATCH = 3
    }

}
