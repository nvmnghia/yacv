package com.uet.nvmnghia.yacv.parser.file

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.helper.NaturalOrderComparator
import com.uet.nvmnghia.yacv.parser.metadata.GenericMetadataParser
import com.uet.nvmnghia.yacv.parser.metadata.MetadataParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import com.uet.nvmnghia.yacv.utils.StringUtils
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
    val document: DocumentFile,
) {

    constructor(context: Context, uri: Uri) :
            this(context, DocumentFile.fromSingleUri(context, uri)!!)    // TODO: Handle #6

    constructor(context: Context, uri: String) : this(context, Uri.parse(uri))

    // Example URI
    // content://com.android.providers.downloads.documents/tree/raw:/storage/emulated/0/Download/...

    val numOfPages: Int
        get() {
            if (pages == null) {
                scanPages()
            }

            return pages!!.size
        }

    /**
     * Parser for the archive.
     */
    private val archiveParser = ArchiveParserFactory.createArchiveParser(context, document)

    /**
     * Check if the content is corrupted (invalid file, no image) or not.
     * Currently, this check is not reliable, and seems to be redundant.
     * It can only be set by methods that scan the whole archive:
     *   - [requestPage]
     *   - [readPage]
     *   -
     * TODO: fully implement this, or remove it.
     */
    var isCorrupted: Boolean? = null
        private set

    /**
     * Control the parsing of metadata. If no metadata is found, set to true,
     * so that subsequent use of metadata doesn't trigger another pointless scan.
     */
    private var noMetadata = false

    /**
     * Number of page.
     * Internally, it is set lazily by [parseInfo].
     */
    var metadata: Comic? = null
        get() {
            return if (isCorrupted == true || noMetadata) {
                null
            } else {
                parseInfo()
                field
            }
        }
        private set

    /**
     * List of paths of comic pages, sorted in display order.
     */
    private var pages: List<String>? = null

    /**
     * Returns a special [PageRequest] that has [PageRequest.isCover] set.
     * Also this method does NOT call [scanPages], unlike [requestPage].
     */
    fun requestCover(): PageRequest {
        return PageRequest(context, document, pages?.getOrNull(0), true)
    }

    /**
     * Given the cover's page path, return an [InputStream] to read it.
     */
    fun readCover(pagePath: String?): InputStream? {
        if (pagePath != null) {
            return readPage(pagePath)
        } else {
            var coverEntry: ArchiveParser.ArchiveEntry? = null

            archiveParser.getEntryIterator().use { entries ->
                for (entry in entries) {
                    if (!FileUtils.isImage(entry.path)) {
                        continue
                    }

                    if (coverEntry == null) {
                        coverEntry = entry
                    } else {
                        if (PATH_COMPARATOR.compare(coverEntry!!.path, entry.path) > 0) {
                            coverEntry = entry
                        }
                    }
                }
            }

            if (coverEntry == null) {
                Log.w("yacv", "Cover not found in file ${document.uri}")
            }

            return coverEntry?.inputStream
        }
    }

    /**
     * Given a 0-based [pageIdx], return a [PageRequest] for the page.
     */
    fun requestPage(pageIdx: Int): PageRequest {
        if (pages == null) {
            scanPages()
        }

        return PageRequest(context, document, pages!![pageIdx])
    }

    /**
     * Given a page path, return an [InputStream] to read that page.
     */
    fun readPage(pagePath: String): InputStream? {
        archiveParser.getEntryIterator().use { entries ->
            for (entry in entries) {
                if (StringUtils.equalBackward(entry.path, pagePath)) {    // Most of the time paths differ at the end.
                    return entry.inputStream
                }
            }
        }

        Log.w("yacv", "Page not found: $pagePath in file ${document.uri}")
        return null
    }

    /**
     * Scan for comic pages and store their paths inside the archive
     * in [pages] in display order. Also check if the archive is corrupted.
     */
    private fun scanPages() {
        val pageEntryPaths = mutableListOf<String>()

        archiveParser.getEntryIterator().use { entries ->
            for (entry in entries) {
                if (FileUtils.isImage(entry.path)) {
                    pageEntryPaths.add(entry.path)
                }
            }
        }

        pages = pageEntryPaths.sortedWith(PATH_COMPARATOR)
        if (pages?.isEmpty() == true) {
            isCorrupted = true
        }
    }

    /**
     * Parse comic metadata into [metadata], also check if the file is corrupted or not.
     */
    private fun parseInfo() {
        Log.d("yacv", "Parsing metadata: ${document.uri}")

        val comic = Comic(document)
        var parsed = false

        archiveParser.getEntryIterator().use { entries ->
            for (entry in entries) {
                if (parsed && isCorrupted != null) {
                    break
                }

                val entryName = StringUtils.nameFromPath(entry.path)
                if (!parsed &&    // Parse once, even if there's several metadata files
                    MetadataParser.isParsableByName(entryName)
                ) {
                    MetadataParser.parseByFilename(entryName, entry.inputStream, comic)
                    parsed = true
                } else if (isCorrupted == null && FileUtils.isImage(entry.path)) {
                    isCorrupted = false
                }
            }

            // TODO: parse by comment, for zip archive
        }

        // The whole archive is scanned, but no image found
        if (isCorrupted == null) {
            isCorrupted = true
        }

        if (!parsed) {
            GenericMetadataParser.parse(comic)
        }

        metadata = comic
    }

    /**
     * Get [InputStream] of the archive.
     */
    fun getInputStream(): InputStream? {
        return context.contentResolver.openInputStream(document.uri)
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
                    // A0001.jpg
                    // B0003-0004.jpg
                    // The first string should be before the second one.
                    // However, it is after because `.` is after `-`.
                    // Therefore extension must be trimmed.
                    o1.substringBeforeLast('.'),
                    o2.substringBeforeLast('.')
                )
            }
        }
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
        val pagePath: String?,
        val isCover: Boolean = false,
    ) {

        // By default Glide uses toString() as cache key, so this is enough.
        // https://bumptech.github.io/glide/tut/custom-modelloader.html#picking-the-key
        override fun toString(): String {
            return "${document.uri}::$pagePath"
        }
    }
}