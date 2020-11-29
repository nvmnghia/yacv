package com.uet.nvmnghia.yacv.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicDao


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

// This class is abstract. Room will provides an implementation of it.
// https://developer.android.com/jetpack/guide#persist-data
@Database(entities = [Comic::class], version = 1)
@TypeConverters(Comic.CalendarConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun comicDao() : ComicDao

    companion object {
        // Versioning is for the whole database, not individual table
        // so it's reasonable to put migration shit here
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite's virtual table cannot be altered :(
//                database.execSQL("""
//                    ALTER TABLE Comic
//                        ADD COLUMN read_count INTEGER
//                        ADD COLUMN love       INTEGER
//                """.trimIndent())
            }
        }
    }
}