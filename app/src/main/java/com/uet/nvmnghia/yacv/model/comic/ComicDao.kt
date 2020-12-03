package com.uet.nvmnghia.yacv.model.comic

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.author.Position


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

@Dao
abstract class ComicDao(private val appDb: AppDatabase) {
    /**
     * Save without checking for foreign keys.
     * Only suitable for internal use.
     */
    @Insert
    protected abstract fun saveUnsafe(comic: Comic): Long

    /**
     * The same as the overloaded method.
     */
    @Insert
    protected abstract fun saveUnsafe(comics: List<Comic>): List<Long>

    /**
     * Save with checking for foreign keys.
     */
    @Transaction
    open    // By default, class methods are final (not overridable)
    fun save(comic: Comic): Long {
        comic.folderId = appDb.folderDao()
            .saveIfNotExisting(comic.parentFolderPath)

        val comicId = saveUnsafe(comic)

        comic.tmpCharacters?.let {
            val characterIds = appDb.characterDao().saveIfAbsent(it.split(','))
            appDb.comicCharacterJoinDao().save(comicId, characterIds)
        }

        comic.tmpGenre?.let {
            val genreIds = appDb.genreDao().saveIfAbsent(it.split(','))
            appDb.comicGenreJoinDao().save(comicId, genreIds)
        }

        comic.tmpWriter?.let {
            val writerIds = appDb.authorDao().saveIfAbsent(it.split(','))
            writerIds.forEach { writerId ->
                appDb.comicAuthorJoinDao().save(comicId, writerId, Position.Writer.id) }
        }

        comic.tmpPenciller?.let {
            val pencillerIds = appDb.authorDao().saveIfAbsent(it.split(','))
            pencillerIds.forEach { pencillerId ->
                appDb.comicAuthorJoinDao().save(comicId, pencillerId, Position.Penciller.id) }
        }

        comic.tmpInker?.let {
            val inkerIds = appDb.authorDao().saveIfAbsent(it.split(','))
            inkerIds.forEach { interId ->
                appDb.comicAuthorJoinDao().save(comicId, interId, Position.Inker.id) }
        }

        comic.tmpColorist?.let {
            val coloristIds = appDb.authorDao().saveIfAbsent(it.split(','))
            coloristIds.forEach { coloristId ->
                appDb.comicAuthorJoinDao().save(comicId, coloristId, Position.Colorist.id) }
        }

        comic.tmpLetterer?.let {
            val lettererIds = appDb.authorDao().saveIfAbsent(it.split(','))
            lettererIds.forEach { lettererId ->
                appDb.comicAuthorJoinDao().save(comicId, lettererId, Position.Letterer.id) }
        }

        comic.tmpEditor?.let {
            val editorIds = appDb.authorDao().saveIfAbsent(it.split(','))
            editorIds.forEach { editorId ->
                appDb.comicAuthorJoinDao().save(comicId, editorId, Position.Editor.id) }
        }

        return comicId
    }

    /**
     * Do the same as the overloaded method.
     * Internally, this method deduplicates before inserting.
     */
    @Transaction
    open fun save(comics: List<Comic>): List<Long> {
        // Normal implementation: use existing save()
        return comics.map { comic -> save(comic) }

        // WIP: Deduplicate, if a future me think it's worth
//        val mapParentFolderToId = appDatabase.folderDao().dedupThenSaveIfNotExist(
//            comics.map { comic -> comic.parentFolderPath })
//        comics.map { comic -> comic.folderId = mapParentFolderToId[comic.parentFolderPath]!! }
//
//        val comicIds = saveUnsafe(comics)
//
//        // Kotlin reduce doesn't have initial, instead use fold
//        val characters = comics.fold(ArrayList<String>()) { characters, comic ->
//            comic.tmpCharacters
//                ?.trim()
//                ?.split(',')
//                ?.let { characters.addAll(it) }
//            characters
//        }
//        val mapCharacterToId = appDatabase.characterDao().dedupThenSaveIfNotExist(characters)
//
//        return comicIds
    }

    // Room is smart: only queries if there's observer
    @Query("SELECT * FROM Comic WHERE ComicID = :comicId")
    abstract fun get(comicId: String): LiveData<Comic>

    @Query("SELECT * FROM Comic")
    abstract fun getAll(): LiveData<List<Comic>>

    @Query("SELECT * FROM Comic WHERE FolderID = :folderId")
    abstract fun getComicsInFolder(folderId: Int): LiveData<List<Comic>>

    @Query("SELECT * FROM Comic WHERE FolderID = :folderId LIMIT 1")
    abstract fun getFirstComicInFolder(folderId: Int): LiveData<Comic>
}