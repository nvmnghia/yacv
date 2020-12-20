package com.uet.nvmnghia.yacv.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.author.AuthorDao
import com.uet.nvmnghia.yacv.model.author.AuthorFts
import com.uet.nvmnghia.yacv.model.author.RoleTable
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.character.CharacterDao
import com.uet.nvmnghia.yacv.model.character.CharacterFts
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.comic.ComicFts
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.folder.FolderDao
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.genre.GenreDao
import com.uet.nvmnghia.yacv.model.genre.GenreFts
import com.uet.nvmnghia.yacv.model.join.*
import com.uet.nvmnghia.yacv.model.series.Series
import com.uet.nvmnghia.yacv.model.series.SeriesDao
import com.uet.nvmnghia.yacv.model.series.SeriesFts
import com.uet.nvmnghia.yacv.utils.RoomUtils


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

// This class is abstract. Room will provides an implementation of it.
// https://developer.android.com/jetpack/guide#persist-data
@Database(entities = [
    Comic::class, ComicFts::class,
    Folder::class,
    Character::class, CharacterFts::class, ComicCharacterJoin::class,
    Genre::class, GenreFts::class, ComicGenreJoin::class,
    Author::class, AuthorFts::class, ComicAuthorJoin::class,
    Series::class, SeriesFts::class,
    RoleTable::class,
], version = 1)
@TypeConverters(RoomUtils.CalendarConverter::class)
abstract class AppDatabase : RoomDatabase() {
    // @formatter:off
    // File-related tables
    abstract fun comicDao() : ComicDao
    abstract fun folderDao(): FolderDao

    // Metadata-related tables
    abstract fun characterDao(): CharacterDao
    abstract fun genreDao()    : GenreDao
    abstract fun authorDao()   : AuthorDao
    abstract fun seriesDao()   : SeriesDao

    // Join tables
    abstract fun comicCharacterJoinDao(): ComicCharacterJoinDao
    abstract fun comicGenreJoinDao()    : ComicGenreJoinDao
    abstract fun comicAuthorJoinDao()   : ComicAuthorJoinDao
    // @formatter:on

    /**
     * Reset database by truncating all tables referencing and referenced by Comic.
     * This leaves metadata-related tables intact.
     */
    fun resetDb() {
        comicCharacterJoinDao().truncate()
        comicGenreJoinDao().truncate()
        comicAuthorJoinDao().truncate()

        comicDao().truncate()
        folderDao().truncate()
    }

    /**
     * Remove not referenced entries metadata-related tables.
     */
    fun cleanDb() {
        TODO("Implement this")
    }
}