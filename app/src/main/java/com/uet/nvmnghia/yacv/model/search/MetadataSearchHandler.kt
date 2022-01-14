package com.uet.nvmnghia.yacv.model.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.utils.parallelForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton


/**
 * This class is a wrapper for search functionalities defined in [MetadataDao].
 * It is very similar to a repository. Is it a good design then?
 */
@Singleton
class MetadataSearchHandler @Inject constructor(
    appDb: AppDatabase,
) {

    private var daos: MutableList<MetadataDao<out Metadata>> = appDb
        .run { mutableListOf(authorDao(), characterDao(), comicDao(), genreDao(), seriesDao(), folderDao()) }
        .apply { sortBy { dao -> DAO_PRECEDENCE.entries.first { (key, _) -> key.isInstance(dao) }.value } }


    /**
     * Given a [query], search the database for it, and return a 2D list of results.
     * Searchable tables are given above.
     * Searched tables are included in [query].
     * If an empty 2D list is ever emitted, then the search ends but nothing is found.
     * Result list from DAO is called a result group, included only if NOT empty.
     * The list of groups is sorted with [METADATA_PRECEDENCE].
     * If [query]'s preview is set, only returns the first [NUM_PREVIEW_MATCH] + 1 results.
     */
    suspend fun search(query: QueryMultipleTypes):
            LiveData<List<List<Metadata>>> = liveData(timeoutInMs = 3000) {

        // Some advantages over manual coroutine + channel:
        // - Incremental update of result list
        // - Automatic cancellation: instead of passing back & forth
        //   scope/job to call cancel(), just unsubscribe, or wait until timeout
        // - Returns List<List<SearchableMetadata>>: instead of returning
        //   both the job and the channel created, just returns a LiveData wrapper
        //
        // Note that this is a bit over-engineered, but the aim is to understand coroutine
        // and find a better way than passing a callback like the below example:
        // https://play.kotlinlang.org/hands-on/Introduction%20to%20Coroutines%20and%20Channels/08_Channels
        // dao.search() is likely to NOT cooperate anyway

        val requiredDaos = daos.slice(query.types)
        val limit =
            if (query.preview) NUM_PREVIEW_MATCH + 1    // Preview 3 matches, the 4th one is used to check
            else Int.MAX_VALUE                          // if there's more
        val results2D = mutableListOf<List<Metadata>>()
        val latch = CountDownLatch(requiredDaos.size)

        withContext(Dispatchers.IO) {
            requiredDaos.parallelForEach { dao ->
                val results = dao.search(query.query, limit)
                var shouldEmit = false

                // TODO: How about making use of the other search function...
                synchronized(results2D) {
                    if (results.isNotEmpty()) {
                        results2D.add(results)
                        results2D.sortBy { li -> li[0].getType() }
                        shouldEmit = true
                    }

                    latch.countDown()
                    if (latch.count == 0L && results2D.size == 0) {
                        shouldEmit = true
                    }
                }

                if (shouldEmit) emit(results2D)
            }
        }
    }

    /**
     * Same as the above, but for [QuerySingleType].
     * Given a [query], search the database for it, and return a 1D list of results.
     */
    suspend fun search(query: QuerySingleType): LiveData<List<Metadata>> = liveData(timeoutInMs = 5000) {
        val requiredDao = daos[query.type]
        val limit =
            if (query.preview) NUM_PREVIEW_MATCH + 1
            else Int.MAX_VALUE

        withContext(Dispatchers.IO) {
            emit(requiredDao.search(query.query, limit))
        }
    }

    /**
     * Given a [metadata], search for comics with that metadata record.
     */
    suspend fun searchComics(metadata: Metadata): LiveData<List<ComicMini>> = liveData(timeoutInMs = 5000) {
        val requiredDao = daos[metadata.getType()]

        withContext(Dispatchers.IO) {
            emit(requiredDao.searchComic(metadata.getID()))
        }
    }

    companion object {
        /**
         * Number of preview match.
         */
        const val NUM_PREVIEW_MATCH = 3
    }

}
