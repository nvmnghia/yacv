package com.uet.nvmnghia.yacv.parser.file

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.helper.NaturalOrderComparator
import com.uet.nvmnghia.yacv.parser.metadata.MetadataParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import java.io.BufferedInputStream
import java.io.InputStream


/**
 * A pull parser interface for comic file, inspired by bubble.
 *
 * Note that this is a pull parser, as described below:
 * https://stackoverflow.com/a/15895283/5959593
 * In short, it does not return a [Comic], and all read/write
 * are operated on the parser instance returned.
 */
class ComicParser(
    private val context: Context,
    val uri: Uri,
) {

    constructor(context: Context, uri: String): this(context, Uri.parse(uri))

    // Example URI
    // content://com.android.providers.downloads.documents/tree/raw:/storage/emulated/0/Download/...

    /**
     * Parser for the archive.
     */
    private val archiveParser = ArchiveParserFactory.createArchiveParser(context, uri)

    /**
     * List of paths of comic pages, sorted in display order.
     */
    var pages: List<String> = archiveParser.getEntryNames()
        .filter { entryName -> FileUtils.isImage(entryName) }
        .sortedWith(PATH_COMPARATOR)
//        .sortedWith(
//            compareBy(object : NaturalOrderComparator<String>() {} ) { page -> page.substringBeforeLast('.') }
//        )

    val metadata: Comic? by lazy { parseInfo() }

    fun requestCover(): PageRequest = requestPage(0)

    /**
     * Given a 0-based [pageIdx], return a [PageRequest] for the page.
     */
    fun requestPage(pageIdx: Int): PageRequest = PageRequest(context, uri, getPageOffset(pageIdx))

    /**
     * Given a 0-based [pageIdx], return the offset of the entry of that page.
     */
    fun getPageOffset(pageIdx: Int): Int = archiveParser.getLayout()[pages[pageIdx]]!!

    /**
     * Given a 0-based [pageIdx], return an [InputStream] to read that page.
     */
    fun readPage(pageIdx: Int): InputStream = archiveParser.getInputStream(pages[pageIdx])

    /**
     * Parse comic metadata into [metadata], also check if the file is corrupted or not.
     */
    private fun parseInfo(): Comic {
        // TODO: Grab comment and parse
        val metadata = Comic(DocumentFile.fromSingleUri(context, uri)!!)

        val metadataFilename = archiveParser.getEntryNames()
            .filterNot { entryName -> FileUtils.isImage(entryName) }
            .firstOrNull { entryName -> MetadataParser.isParsableByName(entryName) }

        if (metadataFilename != null) {
            archiveParser.getInputStream(metadataFilename).use { mis ->
                MetadataParser.parseByFilename(metadata, metadataFilename, mis)
            }
        }

        return metadata
    }

    /**
     * Get the file type of the comic.
     */
    fun getType(): ComicFileType {
        return archiveParser.getType()
    }


    companion object {
        val PATH_COMPARATOR = object : NaturalOrderComparator<String>() {
            override fun compare(o1: String, o2: String): Int {
                return STRING_COMPARATOR.compare(
                    // Special handling: consider 2 strings:
                    // 0001.jpg
                    // 0003-0004.jpg
                    // The first string should be before the second one.
                    // However, it is after because `.` is after `-`.
                    // Therefore extension must be trimmed.
                    o1.substringBeforeLast('.'),
                    o2.substringBeforeLast('.')
                )
            }
        }

        /**
         * Given a [context] and a [uri], get the [InputStream] of the resource,
         * wrapped in a [BufferedInputStream].
         */
        fun getFileInputStream(context: Context, uri: Uri) =
            // https://commons.apache.org/proper/commons-compress/examples.html#Buffering
            // https://developer.android.com/training/data-storage/shared/documents-files#input_stream
            BufferedInputStream(context.contentResolver.openInputStream(uri), 8192)
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
        val uri: Uri,
        val offset: Int
    ) {

        // By default Glide uses toString() as cache key, so this is enough.
        // https://bumptech.github.io/glide/tut/custom-modelloader.html#picking-the-key
        override fun toString(): String {
            return "$uri::$offset"
        }

    }
}