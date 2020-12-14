package com.uet.nvmnghia.yacv.parser.file

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.model.comic.Comic
import java.io.InputStream


/**
 * A pull parser interface for comic file, inspired by bubble.
 * A parser instance should be created by [ComicParserFactory].
 *
 * Note that this is a pull parser, as described below:
 * https://stackoverflow.com/a/15895283/5959593
 * In short, it does not return a [Comic], and all read/write
 * are operated on the parser instance returned.
 */
abstract class ComicParser(val comicDocument: DocumentFile) : AutoCloseable {

    val numOfPages: Int
        get() = this._getNumOfPages()

    val info: Comic by lazy {
        parseInfo()
    }

    /**
     * Check if the given page index is a valid one.
     */
    private fun checkPageIdx(pageIdx: Int) {
        if (pageIdx < 0) {
            throw IndexOutOfBoundsException("Negative page index $pageIdx. Valid range is [0, $numOfPages).")
        } else if (pageIdx > numOfPages) {
            throw IndexOutOfBoundsException("Page index larger than or equal to $numOfPages. Valid range is [0, $numOfPages].")
        }
    }

    fun requestCover(): PageRequest {
        return PageRequest(comicDocument.uri, 0)
    }

    /**
     * Given a 0-based page number, return a [PageRequest] object
     * wrapping the file path and the index. Glide will then load
     * the page of the comic accordingly.
     */
    fun requestPage(pageIdx: Int): PageRequest {
        checkPageIdx(pageIdx)
        return PageRequest(comicDocument.uri, pageIdx)
    }

    /**
     * Given a 0-based page number, return an input stream to read that page.
     *
     * @param pageIdx Page number
     * @return [InputStream] to read that page
     */
    fun readPage(pageIdx: Int): InputStream {
        checkPageIdx(pageIdx)
        return getPageInputStream(pageIdx)
    }

    /**
     * Implementation of [readPage].
     */
    protected abstract fun getPageInputStream(pageIdx: Int): InputStream

    /**
     * Get the number of page of the comic file
     *
     * @return The number of page of the comic
     */
    protected abstract fun _getNumOfPages(): Int

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
     *
     * @return Comic info as a [Comic] instance
     */
    protected abstract fun parseInfo(): Comic


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
        val uri: String,
        val pageIdx: Int
    ) {
        constructor(documentUri: Uri, pageIdx: Int) : this(documentUri.toString(), pageIdx)

        /**
         * Needs a proper serialization, as by default Glide use toString() as cache key.
         */
        override fun toString(): String {
            return "$uri::$pageIdx"
        }
    }
}