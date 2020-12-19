package com.uet.nvmnghia.yacv.parser.file.impl

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.metadata.GenericMetadataParser
import com.uet.nvmnghia.yacv.parser.metadata.MetadataParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import com.uet.nvmnghia.yacv.utils.IOUtils
import com.uet.nvmnghia.yacv.utils.NaturalOrderComparator
import com.uet.nvmnghia.yacv.utils.StringUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


/**
 * Parser for CBZ.
 * There are 2 modes:
 * - Scan mode (null [filePath]): only the [InputStream] of [document]
 *   is available. Whenever [infoIS] or [coverIS] is needed, a new
 *   [InputStream] of the [document] is queried.
 * - Read mode (not-null [filePath]): [document] is copied into
 *   app-specific storage, so that [ZipFile] is backed by
 *   a normal file, enabling random IO.
 * Scan mode is the default mode.
 *
 * TODO: synchronize(this)???
 */
class CBZParser(context: Context, document: DocumentFile) : ComicParser(context, document) {

    /**
     * The current mode of the parser.
     */
    private var MODE: Mode? = Mode.SCAN


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
                    isCorrupted = true
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

    override fun getCoverInputStream(): InputStream? {
        if (MODE == Mode.READ) {
            return getPageInputStream(0)
        } else if (MODE == Mode.SCAN) {
            // https://stackoverflow.com/a/38957431/5959593
            // If a mutable variable is declared outside lambda, smart cast is not available inside lambda.
            // In this particular case:
            // If coverEntry is inside the use block, then in compareString(),
            // there is no need for !!, as coverEntry is smart casted to ZipEntry
            // instead of the current ZipEntry?.
            // However, it is outside the use block, rendering smart cast useless,
            // and coverEntry!!.name is required.
            var coverEntry: ZipEntry? = null

            // Find the cover entry
            ZipInputStream(getInputStream()).use {
                while (true) {
                    val currentEntry = it.nextEntry ?: break

                    if (!FileUtils.isImage(currentEntry.name)) {
                        continue
                    }

                    if (
                        coverEntry == null ||
                        NaturalOrderComparator.STRING_COMPARATOR.compareString(
                            coverEntry!!.name, currentEntry.name) < 1
                    ) {
                        coverEntry = currentEntry
                    }
                }
            }

            if (coverEntry == null) {
                isCorrupted = true    // No image
                return null
            }

            val baos = ByteArrayOutputStream()

            // Read the cover
            ZipInputStream(getInputStream()).use { zis ->
                while (true) {
                    // Once the entry is retrieved, zis is positioned to read the data
                    val currentEntry = zis.nextEntry ?: break

                    if (currentEntry.name == coverEntry!!.name) {
                        IOUtils.copy(zis, baos)
                        baos.flush()
                        break
                    }
                }
            }

            return ByteArrayInputStream(baos.toByteArray())
        } else {
            throw IllegalStateException("CBZParser Mode is not READ or SCAN")
        }
    }

    override fun lazyGetNumOfPages(): Int {
        return pages!!.size
    }

    override fun getTypeEnum(): ComicFileType {
        return ComicFileType.CBZ
    }

    override fun parseInfo(): Comic? {
        Log.w("yacv", "Parsing ${document.uri}")

        val comic = Comic(document.uri)
        var parsed = false

        if (MODE == Mode.READ) {
            if (isCorrupted) {
                return null
            }

            val metadataEntry = zipFile!!.entries()
                .asSequence()
                .firstOrNull { ze ->
                    MetadataParser.checkParsableByName(
                        StringUtils.fileNameFromPath(ze.name)!!)
                }

            MetadataParser.parseByFilename(
                StringUtils.fileNameFromPath(metadataEntry?.name),
                zipFile!!.getInputStream(metadataEntry), comic)
            parsed = true
        } else if (MODE == Mode.SCAN) {
            val zis = ZipInputStream(getInputStream())

            zis.use {
                var currentEntry: ZipEntry?
                while (true) {
                    currentEntry = it.nextEntry
                    if (currentEntry == null) {
                        break
                    }

                    if (MetadataParser.checkParsableByName(
                            StringUtils.fileNameFromPath(currentEntry.name))
                    ) {
                        MetadataParser.parseByFilename(
                            StringUtils.fileNameFromPath(currentEntry.name),
                            it, comic)
                        parsed = true
                        break
                    }
                }
            }
        } else {
            throw IllegalStateException("CBZParser Mode is not READ or SCAN")
        }

        if (!parsed) {
            GenericMetadataParser.parse(comic)
        }

        return comic
    }

    override fun close() {
        if (MODE == Mode.READ) {
            zipFile!!.close()
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