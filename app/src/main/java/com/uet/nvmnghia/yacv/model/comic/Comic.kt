package com.uet.nvmnghia.yacv.model.comic

import androidx.room.*
import java.io.File
import java.util.*


/**
 * This class is a selected subset of what provided in
 * https://github.com/dickloraine/EmbedComicMetadata/blob/master/genericmetadata.py
 * More information about numbering convention for comic
 * https://www.reddit.com/r/comicbooks/comments/k1bqri/numbering_convention_for_comic/
 */

/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */
@Entity
@Fts4(notIndexed = ["language", "bw", "manga", "date", "web"])    // Full Text Search
data class Comic(
    val path: String    // TODO: make sure path is canonical
) {

    constructor(file: File) : this(file.canonicalPath)

    // ID could be omitted for FTS, but if present:
    // - Type: must be Int
    // - Row name: must be "rowid"
    // - SELECT: explicitly mention "rowid"
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
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
//    var love: Boolean = false
//    @ColumnInfo(name = "read_count")
//    var readCount: Int = 0

    class CalendarConverter {
        /**
         * Convert timestamp in millisecond [msTimestamp] to [Calendar].
         */
        @TypeConverter
        fun fromTimestampToCalendar(msTimestamp: Long?): Calendar? {
            if (msTimestamp == null) {
                return null
            }
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = msTimestamp
            return calendar
        }

        /**
         * Convert [Calendar] to timestamp in millisecond.
         */
        @TypeConverter
        fun fromCalendarToTimestamp(calendar: Calendar?): Long? {
            return calendar?.timeInMillis
        }
    }
}
