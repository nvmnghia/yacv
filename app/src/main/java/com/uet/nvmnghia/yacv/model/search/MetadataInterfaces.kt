package com.uet.nvmnghia.yacv.model.search

import android.os.Parcelable
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.author.AuthorDao
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.character.CharacterDao
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.folder.FolderDao
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.genre.GenreDao
import com.uet.nvmnghia.yacv.model.series.Series
import com.uet.nvmnghia.yacv.model.series.SeriesDao


/**
 * Interface for all searchable metadata types/categories:
 * Author, Character, Comic, Folder, Genre, Series.
 * There are 2 special metadata classes called placeholders
 * that eases the rendering of search results:
 * - [com.uet.nvmnghia.yacv.ui.search.ResultGroupHeaderPlaceholder]:
 *   placeholder for result group header
 * - [com.uet.nvmnghia.yacv.ui.search.SeeMorePlaceholder]:
 *   placeholder for See More button
 */
interface Metadata : Parcelable {
    /**
     * ID of the metadata, should be unique among its type.
     */
    fun getID(): Long

    /**
     * Label of the metadata, for display purposes.
     */
    fun getLabel(): String

    /**
     * Type number is also the display precedence of the type.
     * Each metadata type has a fixed type number.
     * This should be a static/companion object method, but currently
     * there's no way to enforce that in interface.
     */
    fun getType(): Int
}


/**
 * Interface for all searchable metadata DAO.
 */
interface MetadataDao<T : Metadata> {
    /**
     * Given a search query [name], search for a match metadata record.
     */
    fun search(name: String, limit: Int = Int.MAX_VALUE): List<T>    // Covariant shit

    /**
     * Given a [id] of a metadata, search for comics with that metadata record.
     */
    fun searchComic(id: Long): List<ComicMini>
}


// Metadata display precedence in search preview
// The index is both precedence value and group/category/type ID
val SORTED_METADATA_DAO = listOf(
    Pair(ComicMini::class, ComicDao::class),
    Pair(Series::class,    SeriesDao::class),
    Pair(Folder::class,    FolderDao::class),
    Pair(Character::class, CharacterDao::class),
    Pair(Author::class,    AuthorDao::class),
    Pair(Genre::class,     GenreDao::class),
)
val METADATA_PRECEDENCE = SORTED_METADATA_DAO.mapIndexed { idx, pair -> pair.first  to idx }.toMap()
val DAO_PRECEDENCE      = SORTED_METADATA_DAO.mapIndexed { idx, pair -> pair.second to idx }.toMap()
