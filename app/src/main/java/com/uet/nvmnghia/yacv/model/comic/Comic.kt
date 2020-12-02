package com.uet.nvmnghia.yacv.model.comic

import androidx.room.*
import com.uet.nvmnghia.yacv.model.folder.Folder
import org.intellij.lang.annotations.Language
import java.io.File
import java.io.IOException
import java.util.*


/**
 * Comic info class.
 *
 * Note that this class should NOT be created directly
 * - A temporary [Comic] should be created by calling [ComicParser]'s
 *   parseInfo(). Without calling this method, no data is ever parsed.
 *   Even then, the data is not persisted in DB, lacking all ID fields
 *   and only suitable for DAO use.
 * - A fully parsed & persisted [Comic] is returned by using [ComicDao]'s
 *   various get() methods (currently returning [LiveData]).
 *
 * This class is a selected subset of what provided in:
 * https://github.com/dickloraine/EmbedComicMetadata/blob/master/genericmetadata.py
 * More information about numbering convention for comic:
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
@Entity(
    indices = [
        Index(value = ["FilePath"], unique = true),
        Index(value = ["FolderID"]),
    ],
    foreignKeys = [
        ForeignKey(entity = Folder::class,
            parentColumns = ["FolderID"],
            childColumns = ["FolderID"]),    // Foreign key doesn't automagically index
    ]
)
data class Comic(
    @ColumnInfo(name = "FilePath")
    val path: String,    // TODO: make sure path is canonical
) {

    constructor(file: File) : this(file.canonicalPath)

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ComicID")
    var id: Long = 0

    // Comic info
    // TODO: Add volume,...
    // @formatter:off
    @ColumnInfo(name = "Series")
    var series    : String?   = null
    @ColumnInfo(name = "Writer")
    var writer    : String?   = null
    @ColumnInfo(name = "Title")
    var title     : String?   = null
    @ColumnInfo(name = "Genre")
    var genre     : String?   = null
    @ColumnInfo(name = "Summary")
    var summary   : String?   = null
    @ColumnInfo(name = "Language")
    var language  : String?   = null
    @ColumnInfo(name = "Publisher")
    var publisher : String?   = null
    @ColumnInfo(name = "BlackAndWhite")
    var bw        : Boolean?  = null
    @ColumnInfo(name = "Manga")
    var manga     : Boolean?  = null
    @ColumnInfo(name = "Date")
    var date      : Calendar? = null
    @ColumnInfo(name = "Web")
    var web       : String?   = null
    // @formatter:on

    // Temporary, as these fields will be split into tables
    @Ignore var tmpCharacters: String? = null

    // File info
    @ColumnInfo(name = "CurrentPage")
    var currentPage: Int = 0

    @ColumnInfo(name = "NumOfPages")
    var numPages: Int = 0

    var format: String? = null

    @ColumnInfo(name = "FolderID")
    var folderId: Long = 0

    // Reading habit
    @ColumnInfo(name = "Love", defaultValue = "0")
    var love: Boolean = false

    @ColumnInfo(name = "ReadCount", defaultValue = "0")
    var readCount: Int = 0

    // https://stackoverflow.com/a/57762552/5959593
    @delegate:Ignore
    val parentFolderPath: String by lazy {
        val parentFolder = File(path).parentFile
            ?: throw IOException("Cannot get parent folder of $path")
        parentFolder.canonicalPath
    }
}
