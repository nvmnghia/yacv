package com.uet.nvmnghia.yacv.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.character.CharacterDao
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.comic.ComicFts
import com.uet.nvmnghia.yacv.model.join.CharacterComicJoin
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.folder.FolderDao
import com.uet.nvmnghia.yacv.model.join.CharacterComicJoinDao
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
    Character::class, CharacterComicJoin::class,
 ], version = 1)
@TypeConverters(RoomUtils.CalendarConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun comicDao(): ComicDao
    abstract fun folderDao(): FolderDao
    abstract fun characterDao(): CharacterDao

    abstract fun characterComicJoinDao(): CharacterComicJoinDao
}