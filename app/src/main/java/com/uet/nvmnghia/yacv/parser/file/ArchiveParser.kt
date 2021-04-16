package com.uet.nvmnghia.yacv.parser.file

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
     * Get an iterator for entries of the archive.
     * Each call creates a NEW iterator.
     * Child classes must guarantee that the iterator skips folders and hidden files.
     */
    fun getEntryIterator(): ArchiveEntryIterator<ArchiveEntry>

    /**
     * Wrapper interface for archive entry.
     */
    interface ArchiveEntry {
        /**
         * Path of the entry in the archive.
         */
        val path: String

        /**
         * Uncompressed size of the entry.
         */
        val size: Long

        /**
         * [InputStream] of the archive entry.
         */
        val inputStream: InputStream
    }

    /**
     * Interface for array entry iterator.
     */
    interface ArchiveEntryIterator<T> : Iterator<T>, AutoCloseable {

        /**
         * Offset of the current entry.
         */
        fun currentEntryOffset(): Long

    }

}