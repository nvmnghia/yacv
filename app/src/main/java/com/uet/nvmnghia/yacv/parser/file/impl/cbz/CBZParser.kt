package com.uet.nvmnghia.yacv.parser.file.impl.cbz

import android.content.Context
import android.net.Uri
import android.util.Log
import com.uet.nvmnghia.yacv.parser.file.ArchiveParser
import com.uet.nvmnghia.yacv.parser.file.ArchiveParser.ArchiveEntryIterator
import com.uet.nvmnghia.yacv.parser.file.ArchiveParser.ArchiveEntry
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import com.uet.nvmnghia.yacv.utils.IOUtils
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream


/**
 * Parser for CBZ.
 */
class CBZParser(
    private val context: Context,
    private val uri: Uri,
) : ArchiveParser {

    override fun extractTo(folder: File) {
        TODO("Not yet implemented")
    }

    override fun getType(): ComicParser.ComicFileType =
        ComicParser.ComicFileType.CBZ

    override fun getEntryIterator(): ArchiveEntryIterator<ArchiveEntry> = object : ArchiveEntryIterator<ArchiveEntry> {

        // https://commons.apache.org/proper/commons-compress/examples.html#Buffering
        // https://developer.android.com/training/data-storage/shared/documents-files#input_stream
        private val buffIS = BufferedInputStream(context.contentResolver.openInputStream(uri), 8192)

        private val zipIS = ZipArchiveInputStream(buffIS)

        /**
         * Current internal ZIP entry.
         */
        private var currentEntry: ZipArchiveEntry? = null
            set(value) {
                field = value

                nextEntryIsFirst = false
            }

        /**
         * Check if [currentEntry] is [next]ed (consumed) yet.
         * - If true, [currentEntry] must be returned in the next [next].
         * - If false, [currentEntry] must be updated in the next [next].
         */
        private var nexted = false

        /**
         * Check if the next entry is the first entry,
         * i.e. [currentEntry] is a non-entry
         * i.e. [currentEntry] is still at initialization value (null).
         * It is set to false whenever [currentEntry] is reassigned.
         */
        private var nextEntryIsFirst = true

        override fun hasNext(): Boolean {
            if (nexted || nextEntryIsFirst) {
                currentEntry = skipExtra(zipIS)
                nexted = false
            }

            return currentEntry != null
        }

        override fun next(): ZipEntry {
            if (!nexted) nexted = true
            else currentEntry = skipExtra(zipIS)

            if (currentEntry == null) {
                throw NoSuchElementException("CBZParser iterator already reached its end")
            }

            Log.d("yacvwtf", currentEntry!!.name)

            return ZipEntry(currentEntry!!.name, currentEntry!!.size, IOUtils.toInputStream(zipIS))
        }

        override fun close() =
            zipIS.close()

        override fun currentEntryOffset(): Long =
            currentEntry?.localHeaderOffset ?: 0

    }

    /**
     * Skip folder & hidden file entries.
     */
    private fun skipExtra(zis: ZipArchiveInputStream): ZipArchiveEntry? {
        do {
            val fileEntry = zis.nextZipEntry ?: return null

            if (!(fileEntry.isDirectory || FileUtils.naiveIsHidden(fileEntry.name))) {
                return fileEntry
            }
        } while (true)
    }

    class ZipEntry(
        override val path: String,
        override val size: Long,
        override val inputStream: InputStream,
    ) : ArchiveEntry

}