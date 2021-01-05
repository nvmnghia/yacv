package com.uet.nvmnghia.yacv.parser.file

import com.uet.nvmnghia.yacv.parser.helper.CloseableIterator
import java.io.File
import java.io.InputStream


/**
 * Interface for comic archive parser.
 */
interface ArchiveParser {
    /**
     * Given a [folder], extract the images into that folder.
     */
    fun extractTo(folder: File)

    /**
     * Get the file type that the parser supports.
     */
    fun getType(): ComicParser.ComicFileType

    /**
     * Get the iterator for entries of the archive.
     * Child class must guarantee that the iterator skips folders and hidden files.
     */
    val entries: CloseableIterator<ArchiveEntry>

    /**
     * Wrapper interface for archive entry.
     */
    interface ArchiveEntry {
        /**
         * Path of the entry in the archive.
         */
        val path: String

        /**
         * [InputStream] of the archive entry.
         */
        val inputStream: InputStream
    }
}