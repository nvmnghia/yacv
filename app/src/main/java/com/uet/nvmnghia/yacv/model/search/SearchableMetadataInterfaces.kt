package com.uet.nvmnghia.yacv.model.search

import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.series.Series


/**
 * Interface for all searchable metadata:
 * Author, Character, Comic, Folder, Genre, Series
 */
interface SearchableMetadata {
    fun getID(): Long

    fun getLabel(): String

    fun getGroupID(): Int
}


/**
 * Interface for all searchable metadata DAO.
 */
interface SearchableMetadataDao<T : SearchableMetadata> {
    // TODO: convert to suspend and check if it is cancellable
    fun search(name: String, limit: Int = Int.MAX_VALUE): List<T>    // Covariant shit
}


// Metadata display precedence in search preview, doubles as group ID
// Currently not used
val sortedMetadata =
    listOf(ComicMini::class, Series::class, Folder::class, Character::class, Author::class, Genre::class)
val METADATA_PRECEDENCE = sortedMetadata.mapIndexed { idx, kclass -> kclass to idx }.toMap()