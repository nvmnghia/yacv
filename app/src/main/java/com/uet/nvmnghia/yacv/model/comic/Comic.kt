package com.uet.nvmnghia.yacv.model.comic

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.*
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.series.Series
import com.uet.nvmnghia.yacv.parser.metadata.GenericMetadataParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import java.util.*

/**
 * Comic info class.
 *
 * Note that this class should NOT be created directly
 * - A temporary [Comic] should be created by calling
 *   [com.uet.nvmnghia.yacv.parser.file.ComicParser.parseInfo].
 *   Without calling this method, no data is ever parsed.
 *   Even then, the data is not persisted in DB, lacking all ID fields
 *   and only suitable for DAO use.
 * - A fully parsed & persisted Comic is returned by using [ComicDao]'s
 *   various get() methods (currently returning [androidx.lifecycle.LiveData]).
 *
 * This class is a selected subset of what provided in:
 * https://github.com/dickloraine/EmbedComicMetadata/blob/master/genericmetadata.py
 * More information about numbering convention for comic:
 * https://www.reddit.com/r/comicbooks/comments/k1bqri/numbering_convention_for_comic/
 * TL;DR:
 *   Wolverine 1982(1) #1
 *   │         │       └─ Number/no (String): issue number, ~ chapter. Note that it is a String.
 *   │         └─ Volume (Int): Several series can have the same name, so they are distinguished by either year or version.
 *   └─ Series (String): Name of the series.
 *   Count (Int): number of issues (Not in the example).
 *
 * Fts table doesn't support UNIQUE, which is a must for storing canonical paths.
 * Methods considered:
 * - (Current) Split into 2 separate tables, exactly like:
 *   https://stackoverflow.com/questions/29815248/full-text-search-example-in-android
 *     + original table: store all fields
 *     + Fts table:      store Fts fields (Fts fields seem to be stored twice, but in fact they're not)
 *   Drawbacks: complicated linking (2 separated tables) IF NOT using Room, INSERT seems to be slower
 *              needs JOIN the original & Fts table when search with MATCH to enjoy the speedup
 * - Split into 2 tables
 *     + `comic`:    store id & path: use UNIQUE on path
 *     + `metadata`: store the rest:  Fts4 enabled
 *   Drawbacks: even more complicated & manual linking
 * - Normal table + Trigger check instead of indexing
 *     + Trigger on every INSERT
 *     + Trigger doesn't work for Fts (manual index is 1 lines of Room annotation though)
 *   Drawbacks: slowish as Fts is not used
 * - Fts table + app-side unique check
 *   Drawbacks: even more slowish (1 queries to check, 1 to insert)
 *
 * Other downsides of Fts:
 * - rowid/docid cannot be used as foreign key:
 *   https://stackoverflow.com/a/62365525/5959593
 * - Only store strings.
 * - Update in content table, but search in Fts table (that's why JOIN is needed in the current method).
 *   Predictable (since Room has to use trigger to sync the 2 tables) but still surprising.
 *
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */
@Entity(
    indices = [
        Index(value = [Comic.COLUMN_COMIC_URI], unique = true),
        Index(value = [Folder.COLUMN_FOLDER_ID]),    // Foreign key doesn't automagically index
        Index(value = [Series.COLUMN_SERIES_ID]),
    ],
    foreignKeys = [
        ForeignKey(entity = Folder::class,    // Referenced entity is parent
            parentColumns = [Folder.COLUMN_FOLDER_ID],
            childColumns = [Folder.COLUMN_FOLDER_ID]),
        ForeignKey(entity = Series::class,
            parentColumns = [Series.COLUMN_SERIES_ID],
            childColumns = [Series.COLUMN_SERIES_ID]),
    ]
)
class Comic(
    @ColumnInfo(name = COLUMN_COMIC_URI)
    val fileUri: String,
) {

    constructor(document: DocumentFile) : this(document.uri.toString())


    //================================================================================
    // Room fields - Metadata
    //================================================================================

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_COMIC_ID)
    var id: Long = 0

    // TODO: Add volume,...
    // @formatter:off
    @ColumnInfo(name = Series.COLUMN_SERIES_ID)
    var seriesId : Long?     = null
    @ColumnInfo(name = "Number")
    var number   : Int?      = null
    @ColumnInfo(name = COLUMN_TITLE)
    var title    : String    = Uri.parse(fileUri)
        .lastPathSegment!!
        .substringBeforeLast('.')
    @ColumnInfo(name = COLUMN_SUMMARY)
    var summary  : String?   = null
    @ColumnInfo(name = "Language")
    var language : String?   = null
    @ColumnInfo(name = COLUMN_PUBLISHER)
    var publisher: String?   = null
    @ColumnInfo(name = "BlackAndWhite")
    var bw       : Boolean?  = null
    @ColumnInfo(name = "Date")
    var date     : Calendar? = null
    @ColumnInfo(name = "Web")
    var web      : String?   = null
    // @formatter:on


    //================================================================================
    // Room fields - Management
    //================================================================================

    // File info
    @ColumnInfo(name = "CurrentPage")
    var currentPage: Int = 0

    @ColumnInfo(name = "NumOfPages")
    var numPages: Int = 0

    @ColumnInfo(name = Folder.COLUMN_FOLDER_ID)
    var folderId: Long = 0


    // Reading habit
    @ColumnInfo(name = "Love", defaultValue = "0")
    var love: Boolean = false

    @ColumnInfo(name = "ReadCount", defaultValue = "0")
    var readCount: Int = 0


    //================================================================================
    // Non-Room fields
    //================================================================================

    // Temporary, as these fields will be split into tables
    // @formatter:off
    @Ignore var tmpCharacters : String?  = null
    @Ignore var tmpGenre      : String?  = null
    @Ignore var tmpWriter     : String?  = null
    @Ignore var tmpEditor     : String?  = null
    @Ignore var tmpPenciller  : String?  = null
    @Ignore var tmpInker      : String?  = null
    @Ignore var tmpColorist   : String?  = null
    @Ignore var tmpLetterer   : String?  = null
    @Ignore var tmpCoverArtist: String?  = null
    @Ignore var tmpVolume     : Int?     = null
    @Ignore var tmpCount      : Int?     = null
    @Ignore var tmpManga      : Boolean? = null
    // @formatter:on

    /**
     * Temporary hold series name.
     * This field is important, as the file name is very likely to be the series name.
     * When initialized using the constructor, [tmpSeries] is null.
     * The [Comic] instance must then be passed to [GenericMetadataParser.parse]
     * to fill this field.
     * TODO: avoid the explicit call to GenericMetadataParser.
     */
    @Ignore
    var tmpSeries: String? = null

    @Ignore
    lateinit var tmpFolderUri: String

    /**
     * Sometimes folder name is difficult to naively parse from [tmpFolderUri].
     * So it's best to save folder name in [Folder].
     */
    @Ignore
    var tmpFolderName: String

    /**
     * Check if the comic is non-generically parsed.
     * Only used when parsing.
     */
    @Ignore
    var nonGenericallyParsed = false


    init {
        tmpFolderUri = Uri.decode(fileUri).substringBeforeLast('/')
        tmpFolderName = Uri.decode(fileUri).substringAfterLast(':').substringBeforeLast('/')
    }


    companion object {
        // Column names
        // @formatter:off
        const val COLUMN_COMIC_ID  = "ComicID"
        const val COLUMN_COMIC_URI = "FileUri"

        internal const val COLUMN_TITLE     = "Title"
        internal const val COLUMN_SUMMARY   = "Summary"
        internal const val COLUMN_PUBLISHER = "Publisher"
        internal const val COLUMN_WEB       = "Web"
        // @formatter:on
    }

}
