package com.uet.nvmnghia.yacv.parser.file.impl

import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.metadata.comicrack.ComicRackParser
import com.uet.nvmnghia.yacv.parser.metadata.generic.GenericMetadataParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import com.uet.nvmnghia.yacv.utils.NaturalOrderComparator
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


/**
 * Parser for CBZ.
 * There're 2 modes:
 * - Scan mode (null [filePath]): only the [InputStream] of [document]
 *   is available. Whenever [infoIS] or [coverIS] is needed, a new
 *   [InputStream] of the [document] is queried.
 * - Read mode (not-null [filePath]): [document] is copied into
 *   app-specific storage, so that [ZipFile] is backed by
 *   a normal file, enabling random IO.
 * Scan mode is the default mode.
 */
class CBZParser(document: DocumentFile) : ComicParser(document) {

    /**
     * The current mode of the parser.
     */
    private var MODE: Mode? = Mode.SCAN

    /**
     * Check if the content is corrupted or not.
     */
    var CORRUPTED = false
        private set

    //================================================================================
    // Read mode
    //================================================================================

    /**
     * Path to a copy of the archive in app-specific storage.
     * Only available in Read mode.
     *
     * TODO: Take advantage of Glide caching to avoid copy the whole archive
     *  as images itself are already compressed.
     */
    var filePath: String? = null
        set(value) {
            if (value != null) {
                field = value
                MODE = Mode.READ

                // TODO: make this lazy
                try {
                    zipFile = ZipFile(filePath)
                } catch (ioe: IOException) {
                    CORRUPTED = true
                }
            }
        }

    /**
     * [ZipFile] of the specified file, backed by [filePath].
     * Only available in Read mode.
     * TODO: copy to app-specific storage when needed.
     */
    private var zipFile: ZipFile? = null
        set(value) {
            field = value

            // TODO: make this lazy
            pages = field?.entries()
                ?.toList()
                ?.filter { entry -> !entry.isDirectory && FileUtils.isImage(entry.name) }
                ?.sortedWith(COMPARATOR)
        }

    /**
     * Pages as [ZipEntry] in [zipFile].
     * Only available in Read mode.
     */
    private var pages: List<ZipEntry>? = null


    //================================================================================
    // Scan mode
    //================================================================================

    /**
     * [InputStream] of info file.
     * Only available in Scan mode.
     */
    private var infoIS: InputStream? = null

    /**
     * [InputStream] of cover file.
     * Only available in Scan mode.
     */
    private var coverIS: InputStream? = null


    //================================================================================
    // Functions
    //================================================================================

    override fun getPageInputStream(pageIdx: Int): InputStream {
        return zipFile!!.getInputStream(pages!![pageIdx])
    }

    override fun getCoverInputStream(): InputStream {
        TODO("Not yet implemented")
    }

    override fun lazyGetNumOfPages(): Int {
        return pages!!.size
    }

    override fun getTypeEnum(): ComicFileType {
        return ComicFileType.CBZ
    }

    override fun parseInfo(): Comic? {
        // First use a generic parser
        val comic = GenericMetadataParser.parse(document)

        // Then the more sophisticated one
        if (MODE == Mode.READ) {
            if (!CORRUPTED) {    // Valid zip file
                val comicInfoXml = zipFile!!.entries()
                    .asSequence()
                    .firstOrNull { entry -> entry.name.toLowerCase(Locale.ROOT) == "comicinfo.xml" }

                if (comicInfoXml != null) {
                    ComicRackParser.parse(zipFile!!.getInputStream(comicInfoXml), comic)
                }
            } else {
                return null
            }
        } else if (MODE == Mode.SCAN) {
//            TODO("No document ")
        } else {
            throw IllegalStateException("Parser Mode is not READ or SCAN")
        }

        return comic
    }

    override fun close() {
        if (MODE == Mode.READ) {
            zipFile!!.close()
        } else {
//            TODO("No input stream yet")
        }
    }

    companion object {
        val COMPARATOR = object : NaturalOrderComparator<ZipEntry>() {
            override fun compare(o1: ZipEntry, o2: ZipEntry): Int {
                return STRING_COMPARATOR.compare(
                    // Special handling: consider 2 strings:
                    // A0001.jpg
                    // B0003-0004.jpg
                    // The first string should be before the second one.
                    // However, it is after because `.` is after `-`.
                    // Therefore extension must be trimmed.
                    o1.name.substringBeforeLast('.'),
                    o2.name.substringBeforeLast('.')
                )
            }
        }
    }


    private enum class Mode {
        SCAN, READ
    }
}