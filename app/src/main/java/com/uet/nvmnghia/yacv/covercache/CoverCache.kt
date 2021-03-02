package com.uet.nvmnghia.yacv.covercache

import android.content.Context
import android.graphics.BitmapFactory
import com.uet.nvmnghia.yacv.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Manually cache some low-resolution covers.
 *
 * The cache key is the comic ID.
 * A LRU cache of at most 50MB.
 *
 * The cache image has the following properties:
 * - Extension: JPEG
 * - Name: the key itself
 * - Width: one third the screen size at most
 * - Color: RGB888
 * - Quality: 75
 */
@Singleton
class CoverCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Folder used to cache low-resolution covers.
     */
    private lateinit var lowResCoverFolder: File

    /**
     * Keep track of current cache size in byte.
     */
    private var currentSize: Long = 0L

    /**
     * Max cache size in MB.
     */
    private val MAX_CACHE_SIZE = 50 * 1024 * 1024

    private val MAX_WIDTH = context.resources.displayMetrics.widthPixels / 3
    private val MAX_QUALITY = 75

    /**
     * The main LRU cache.
     * accessOrder must be true to simulate LRU behavior.
     */
    private var lruCache = object : LinkedHashMap<Long, File>(
        50, .75f, true) {

        override fun put(key: Long, value: File): File? {
            val putResult = super.put(key, value)

            currentSize += value.length()

            return putResult
        }

        override fun removeEldestEntry(eldest: Map.Entry<Long, File>?): Boolean {
            if (currentSize > MAX_CACHE_SIZE) {
                currentSize -= eldest?.value?.length() ?: 0
                return true
            }

            return false
        }
    }

    init {
        initLowResCoverFolder()
        buildCache()
    }

    /**
     * Cache a cover if not already cached.
     * [glideCache] is the cache file in Glide.
     * Note that the [File] returned is NOT GUARANTEED to be ready.
     * This method will compress it, and write output to that file,
     * just not immediately.
     */
    fun cache(comicID: Long, glideCache: File): File? {
        val toBeCached = File(lowResCoverFolder, "$comicID.jpg")
        if (toBeCached.exists()) {
            return toBeCached
        }

        if (getOriginalWidth(glideCache) <= MAX_WIDTH) {
            FileUtils.copy(glideCache, toBeCached)
            return toBeCached
        }

        CoroutineScope(Dispatchers.IO).launch {
            Compressor.compress(context, glideCache) {
                default(width = MAX_WIDTH)
                quality(MAX_QUALITY)
                destination(toBeCached)
            }
        }

        return null
    }

    /**
     * Get the width of the original file.
     */
    private fun getOriginalWidth(glideCache: File): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(glideCache.canonicalPath, options)

        return options.outWidth
    }

    /**
     * Get cover cache directory. Create if not exists.
     */
    private fun initLowResCoverFolder() {
        lowResCoverFolder = File(context.getExternalFilesDir(null), "manual_cache")

        // Check existence before mkdir is an anti-pattern
        // https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java#comment30074885_3634906
        lowResCoverFolder.mkdirs()
    }

    /**
     * Scan [lowResCoverFolder] and put it in [lruCache].
     */
    private fun buildCache() {
        lowResCoverFolder.listFiles()?.forEach { cover ->
            cover.name
                .substringBeforeLast('.')
                .toLongOrNull()
                ?.let { key -> lruCache[key] = cover }
        }
    }

}