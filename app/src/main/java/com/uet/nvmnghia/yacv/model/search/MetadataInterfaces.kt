package com.uet.nvmnghia.yacv.model.search

import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.series.Series


/**
 * Interface for all searchable metadata types/categories:
 * Author, Character, Comic, Folder, Genre, Series
 */
interface Metadata {
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
     */
    fun getType(): Int
}


/**
 * Interface for all searchable metadata DAO.
 */
interface MetadataDao<T : Metadata> {
    fun search(name: String, limit: Int = Int.MAX_VALUE): List<T>    // Covariant shit
}


// Metadata display precedence in search preview, doubles as group ID
// Currently not used
val SORTED_METADATA = listOf(
    ComicMini::class, Series::class, Folder::class, Character::class, Author::class, Genre::class)
val METADATA_PRECEDENCE = SORTED_METADATA.mapIndexed { idx, kclass -> kclass to idx }.toMap()