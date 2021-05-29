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
     * Get list of entry names.
     * Child classes must guarantee that the iterator skips folders and hidden files.
     */
    fun getEntryNames(): List<String>

    /**
     * Given an [entryName], get its [InputStream].
     * The [InputStream] must be created anew in each call.
     */
    fun getInputStream(entryName: String): InputStream

    /**
     * Get the map of entry name/internal path to offset.
     */
    fun getLayout(): Map<String, Int>

}