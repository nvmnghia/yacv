package com.uet.nvmnghia.yacv.parser.file

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.model.comic.Comic
import java.io.File
import java.io.InputStream


/**
 * A pull parser interface for comic file, inspired by bubble.
 * The parser should be created by [ComicParserFactory].
 *
 * Note that this is a pull parser, as described below:
 * https://stackoverflow.com/a/15895283/5959593
 * In short, it does not return a [Comic], and all read/write
 * are operated on the parser instance returned.
 */
abstract class ComicParser(private val context: Context, val document: DocumentFile) : AutoCloseable {

    // Example URI
    // content://com.android.providers.downloads.documents/tree/raw:/storage/emulated/0/Download/...

    val numOfPages: Int by lazy {
        lazyGetNumOfPages()
    }

    /**
     * Check if the content is corrupted (invalid file, no image) or not.
     * TODO: fully implement this.
     */
    var isCorrupted = false
        protected set

    val info: Comic? by lazy {
        parseInfo()
    }

    fun requestCover(): PageRequest {
        return PageRequest(context, document, PageRequest.COVER)
    }

    /**
     * Given a 0-based page number [pageIdx], return a [PageRequest]
     * wrapping the file path and the index.
     * Glide will then load the page of the comic accordingly.
     */
    fun requestPage(pageIdx: Int): PageRequest {
        checkPageIdx(pageIdx)
        return PageRequest(context, document, pageIdx)
    }

    /**
     * Given a 0-based page number [pageIdx], return an [InputStream] to read that page.
     * The page number can also be [PageRequest.COVER], which is not in the normal range.
     *
     * @param pageIdx Page number
     * @return [InputStream] to read that page
     */
    fun readPage(pageIdx: Int): InputStream? {
        return if (pageIdx == PageRequest.COVER) {
            getCoverInputStream()
        } else {
            checkPageIdx(pageIdx)
            getPageInputStream(pageIdx)
        }
    }

    /**
     * Check if the given page index [pageIdx] is a valid one.
     */
    private fun checkPageIdx(pageIdx: Int) {
        if (pageIdx < 0) {
            throw IndexOutOfBoundsException("Negative page index $pageIdx. Valid range is [0, $numOfPages).")
        } else if (pageIdx >= numOfPages) {
            throw IndexOutOfBoundsException("Page index larger than or equal to $numOfPages. Valid range is [0, $numOfPages).")
        }
    }

    /**
     * Implementation of [readPage].
     */
    protected abstract fun getPageInputStream(pageIdx: Int): InputStream?

    /**
     * Special handling of [readPage] for cover page.
     * Cover page is just the page with pageIdx = 0, but as it is the lowest index,
     * it can be retrieved very fast, if the format permits.
     * Otherwise, just fall back to [getPageInputStream] with pageIdx = 0.
     */
    protected abstract fun getCoverInputStream(): InputStream?

    /**
     * Get the number of page of the comic file.
     * This function is evaluated once.
     *
     * @return The number of page of the comic
     */
    protected abstract fun lazyGetNumOfPages(): Int

    /**
     * Get the file type of the comic.
     *
     * @return [ComicFileType] of the comic.
     */
    abstract fun getTypeEnum(): ComicFileType

    /**
     * Get the file type of the comic.
     *
     * @return File type of the comic, in lowercase string
     */
    fun getType(): String {
        return getTypeEnum().extension
    }

    /**
     * Get comic info.
     * If the file is invalid (corrupt, no image), return null.
     * TODO: Force check invalid file (corrupt, no image), even if the check if non-exhaustive.
     *
     * @return Comic info as a [Comic] instance
     */
    protected abstract fun parseInfo(): Comic?

    /**
     * Get [InputStream] of the archive.
     */
    fun getInputStream(): InputStream? {
        return context.contentResolver.openInputStream(document.uri)
    }

    /**
     * Copy to app-specific storage.
     */
    private fun copyToAppSpecific(): File {
        TODO("Not fucking implemented")
    }


    /**
     * Enum class for file types.
     */
    enum class ComicFileType(val extension: String) {
        CBZ("cbz"),
//        CBR("cbr")
    }


    /**
     * Bundle of comic file path and index of the page to read from that file.
     */
    data class PageRequest(
        val context: Context,
        val document: DocumentFile,
        val pageIdx: Int
    ) {

        companion object {
            const val COVER = -1
        }

        /**
         * Needs a proper serialization, as by default Glide use toString() as cache key.
         */
        override fun toString(): String {
            return "${document.uri}::$pageIdx"
        }
    }
}