package com.uet.nvmnghia.yacv.covercache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.uet.nvmnghia.yacv.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.shouheng.compress.Compress
import me.shouheng.compress.naming.CacheNameFactory
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.strategy.config.ScaleMode
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

    private val WIDTH = context.resources.displayMetrics.widthPixels.toFloat() / 5
    private val QUALITY = 60

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
     * This function opportunistically takes advantage of [glideCache] - the newly cached file in Glide.
     * Note that the [File] returned is GUARANTEED to be ready, otherwise it just returns null.
     * TODO: glideCache is not always available
     */
    fun cache(comicID: Long, glideCache: File? = null): File? {
        val toBeCached = File(lowResCoverFolder, getCacheFileName(comicID))
        if (toBeCached.exists()) {
            return toBeCached
        }

        if (glideCache == null) {
            return null
        }

        if (getOriginalWidth(glideCache) <= WIDTH) {
            FileUtils.copy(glideCache, toBeCached)
            return toBeCached
        }

        Log.d("yacvwtf", "Caching low res for comicID = $comicID")

        CoroutineScope(Dispatchers.IO).launch {
            Compress.with(context, glideCache)
                .setTargetDir(lowResCoverFolder.canonicalPath)
                .setCacheNameFactory(getCacheNameFactory(comicID))
                .setQuality(QUALITY)
                .strategy(Strategies.compressor())
                .setMaxWidth(WIDTH)
                .setScaleMode(ScaleMode.SCALE_WIDTH)
                .get(Dispatchers.IO)
        }

        return null
    }

    private fun getCacheFileName(comicID: Long): String =
        "$comicID.jpg"

    private fun getCacheNameFactory(comicID: Long) = object : CacheNameFactory {
        override fun getFileName(format: Bitmap.CompressFormat): String =
            when (format) {
                Bitmap.CompressFormat.JPEG -> getCacheFileName(comicID)
                else -> throw IllegalStateException("JPEG cache only.")
            }
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

    fun getLowResCacheFolder() = lowResCoverFolder

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