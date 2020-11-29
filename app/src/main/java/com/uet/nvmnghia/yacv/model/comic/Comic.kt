package com.uet.nvmnghia.yacv.model.comic

import androidx.room.*
import java.io.File
import java.util.*


/**
 * This class is a selected subset of what provided in
 * https://github.com/dickloraine/EmbedComicMetadata/blob/master/genericmetadata.py
 * More information about numbering convention for comic
 * https://www.reddit.com/r/comicbooks/comments/k1bqri/numbering_convention_for_comic/
 *
 * Fts table doesn't support UNIQUE, which is a must for storing canonical paths.
 * Methods considered:
 * - (Current) Split into 2 tables, exactly like:
 *   https://stackoverflow.com/questions/29815248/full-text-search-example-in-android
 *     + original table: store all fields
 *     + Fts table:      store Fts fields (Fts fields seem to be stored twice, but in fact they're not)
 *   Drawbacks: complicated linking (2 separated tables) IF not using Room, INSERT seems to be slower
 * - Split into 2 tables, inspired by the above SO
 *     + `comic`:    store id & path: use UNIQUE on path
 *     + `metadata`: store the rest:  Fts4 enabled
 *   Drawbacks: even more complicated & manual linking
 * - Normal table + Trigger check
 *     + Trigger on every INSERT
 *     + Trigger doesn't work for Fts (manual index is 1 lines of Room annotation)
 *   Drawbacks: slowish
 * - Fts table + app-side check
 *   Drawbacks: even more slowish
 */

/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */
@Entity(indices = [Index(value = ["path"], unique = true)])
data class Comic(
    val path: String    // TODO: make sure path is canonical
) {

    constructor(file: File) : this(file.canonicalPath)

    // ID could be omitted for FTS, but if present:
    // - Type: must be Int
    // - Row name: must be "rowid"
    // - SELECT: explicitly mention "rowid"
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    // Comic info
    // TODO: Add volume,...
    // @formatter:off
    var series    : String?   = null
    var writer    : String?   = null
    var title     : String?   = null
    var genre     : String?   = null
    var summary   : String?   = null
    var characters: String?   = null
    var language  : String?   = null
    var publisher : String?   = null
    var bw        : Boolean?  = null
    var manga     : Boolean?  = null
    var date      : Calendar? = null
    var web       : String?   = null
    // @formatter:on

    // File info
    @ColumnInfo(name = "current_page")
    var currentPage: Int = 0
    @ColumnInfo(name = "num_pages")
    var numPages: Int = 0
    var format: String? = null

    // Reading habit
    @ColumnInfo(defaultValue = "0")
    var love: Boolean = false
    @ColumnInfo(name = "read_count", defaultValue = "0")
    var readCount: Int = 0
}
