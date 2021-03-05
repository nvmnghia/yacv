package com.uet.nvmnghia.yacv.parser.metadata

import com.uet.nvmnghia.yacv.model.comic.Comic
import java.io.InputStream


/**
 * Interface for non-generic metadata parser.
 * Non-generic parser will parse metadata from a [InputStream] to a given [Comic] instance.
 */
interface INonGenericMetadataParser {
    /**
     * Given a file name, check if the file is parsable by this parser.
     * If [lowered] is set, no string lowercasing is performed. Default to false.
     */
    fun isParsableByName(metadataFilename: String?, lowered: Boolean = false): Boolean

    /**
     * Parse metadata from [mis] into [comic].
     * A [Comic] instance must be provided, instead of returned,
     * as [Comic] requires more information than the input stream
     * (uri for example).
     *
     * @param mis [InputStream] for metadata file to be parsed
     * @param comic The [Comic] instance that will be populated
     * @return true if parsing complete, false otherwise
     */
    fun parse(mis: InputStream, comic: Comic): Boolean
}