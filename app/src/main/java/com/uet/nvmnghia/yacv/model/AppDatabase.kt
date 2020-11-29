package com.uet.nvmnghia.yacv.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.comic.ComicFts
import com.uet.nvmnghia.yacv.utils.RoomUtils


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

// This class is abstract. Room will provides an implementation of it.
// https://developer.android.com/jetpack/guide#persist-data
@Database(entities = [Comic::class, ComicFts::class], version = 2)
@TypeConverters(RoomUtils.CalendarConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun comicDao() : ComicDao

    companion object {
        // Versioning is for the whole database, not individual table
        // so it's reasonable to put migration shit here
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create v2 table...
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS new_Comic (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        series TEXT,
                        writer TEXT,
                        title TEXT,
                        genre TEXT,
                        summary TEXT,
                        characters TEXT,
                        language TEXT,
                        publisher TEXT,
                        bw INTEGER,
                        manga INTEGER,
                        date INTEGER,
                        web TEXT,
                        current_page INTEGER NOT NULL,
                        num_pages INTEGER NOT NULL,
                        format TEXT,
                        love INTEGER NOT NULL DEFAULT 0,
                        read_count INTEGER NOT NULL DEFAULT 0,
                        path TEXT NOT NULL
                    )
                """.trimIndent())

                // ... and index it
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_Comic_path ON new_Comic (path)
                """.trimIndent())

                // Copy data over
                database.execSQL("""
                    INSERT INTO new_Comic (
                        id, series, writer, title, genre, summary, characters, language, publisher, bw, manga, date, web, current_page, num_pages, format, path
                    ) SELECT
                        rowid, series, writer, title, genre, summary, characters, language, publisher, bw, manga, date, web, current_page, num_pages, format, path
                    FROM Comic
                """.trimIndent())

                // SQLite's virtual table cannot be altered. We'll have to drop it anyway.
                database.execSQL("DROP TABLE Comic")

                database.execSQL("ALTER TABLE new_Comic RENAME TO comic")

                // Create Fts shit...
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS ComicFts USING FTS4(
                        series TEXT,
                        writer TEXT,
                        title TEXT,
                        genre TEXT,
                        summary TEXT,
                        characters TEXT,
                        language TEXT,
                        publisher TEXT,
                        bw INTEGER,
                        manga INTEGER,
                        date INTEGER,
                        web TEXT,
                        content=`Comic`)    /* This pair of backtick must be kept */
                """.trimIndent())

                // ... and its sync trigger
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ComicFts_BEFORE_UPDATE BEFORE UPDATE ON Comic
                        BEGIN DELETE FROM ComicFts WHERE docid=OLD.rowid; END
                """.trimIndent())
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ComicFts_BEFORE_DELETE BEFORE DELETE ON Comic
                        BEGIN DELETE FROM ComicFts WHERE docid=OLD.rowid; END
                """.trimIndent())
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ComicFts_AFTER_UPDATE AFTER UPDATE ON Comic
                        BEGIN
                            INSERT INTO ComicFts(
                                docid, series, writer, title, genre, summary, characters, language, publisher, bw, manga, date, web
                            ) VALUES (
                                NEW.rowid, NEW.series, NEW.writer, NEW.title, NEW.genre, NEW.summary, NEW.characters, NEW.language, NEW.publisher, NEW.bw, NEW.manga, NEW.date, NEW.web
                            );
                        END
                """.trimIndent())
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ComicFts_AFTER_INSERT AFTER INSERT ON Comic
                        BEGIN
                            INSERT INTO ComicFts(
                                docid, series, writer, title, genre, summary, characters, language, publisher, bw, manga, date, web
                            ) VALUES (
                                NEW.rowid, NEW.series, NEW.writer, NEW.title, NEW.genre, NEW.summary, NEW.characters, NEW.language, NEW.publisher, NEW.bw, NEW.manga, NEW.date, NEW.web
                            );
                        END
                """.trimIndent())
            }
        }
    }
}