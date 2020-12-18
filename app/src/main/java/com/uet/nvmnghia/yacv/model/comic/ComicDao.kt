package com.uet.nvmnghia.yacv.model.comic

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.author.Role


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
        // Insert into tables that Comic refers to
        comic.folderId = appDb.folderDao().saveIfAbsent(comic.parentFolderPath)
        comic.seriesId = appDb.seriesDao().saveIfAbsent(comic.tmpSeries!!)

        // Insert into Comic
        val comicId = saveUnsafe(comic)

        // Insert into tables that have join tables with Comic
        comic.tmpCharacters?.let {
            val characterIds = appDb.characterDao().saveIfAbsent(it.split(LIST_SEPARATOR))
            appDb.comicCharacterJoinDao().save(comicId, characterIds)
        }

        comic.tmpGenre?.let {
            val genreIds = appDb.genreDao().saveIfAbsent(it.split(LIST_SEPARATOR))
            appDb.comicGenreJoinDao().save(comicId, genreIds)
        }

        // Note: the order of item between 2 lists must be consistent
        val authors   = listOf(comic.tmpWriter, comic.tmpEditor, comic.tmpPenciller,
            comic.tmpInker, comic.tmpColorist, comic.tmpLetterer, comic.tmpCoverArtist)
        val positions = listOf(Role.Writer, Role.Editor, Role.Penciller,
            Role.Inker, Role.Colorist, Role.Letterer, Role.CoverArtist)
        authors.zip(positions) { authorGroup, position ->
            authorGroup?. let {
                val authorIds = appDb.authorDao().saveIfAbsent(authorGroup.split(LIST_SEPARATOR))
                authorIds.forEach { authorId ->
                    appDb.comicAuthorJoinDao().save(comicId, authorId, position.id) }
            }
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
    abstract fun getFirstComicInFolder(folderId: Long): Comic

    @Query("DELETE FROM Comic")
    abstract fun truncate()

    companion object {
        const val LIST_SEPARATOR = ','
    }
}