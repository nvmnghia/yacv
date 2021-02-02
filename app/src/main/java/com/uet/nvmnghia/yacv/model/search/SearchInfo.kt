package com.uet.nvmnghia.yacv.model.search

import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.genre.Genre


/**
 * Interface for all searchable metadata:
 * Author, Character, Comic, Folder, Genre, Series
 */
interface SearchableMetadata {
    fun getID(): Long

    fun getLabel(): String
}


fun AppDatabase.search(term: String): List<SearchableMetadata> {
    return listOf(Genre("abc"))
}