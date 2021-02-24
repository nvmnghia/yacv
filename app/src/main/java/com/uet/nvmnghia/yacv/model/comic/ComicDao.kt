package com.uet.nvmnghia.yacv.model.comic

import androidx.lifecycle.LiveData
import androidx.room.*
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.author.Role
import com.uet.nvmnghia.yacv.model.search.MetadataDao


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

@Dao
abstract class ComicDao(private val appDb: AppDatabase) : MetadataDao<ComicMini> {
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
    fun saveIfAbsent(comic: Comic): Long {
        val ids = getExistingId(comic.fileUri)
        if (ids.isNotEmpty()) {
            return ids[0]
        }

        // Insert into tables that Comic refers to
        comic.folderId = appDb.folderDao().saveIfAbsent(comic.tmpFolderUri, comic.tmpFolderName)
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

        // Note: the order of items between 2 lists must be consistent
        val authors = listOf(comic.tmpWriter, comic.tmpEditor, comic.tmpPenciller,
            comic.tmpInker, comic.tmpColorist, comic.tmpLetterer, comic.tmpCoverArtist)
        val roles   = listOf(Role.Writer, Role.Editor, Role.Penciller,
            Role.Inker, Role.Colorist, Role.Letterer, Role.CoverArtist)
        authors.zip(roles) { authorGroup, position ->
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
    open fun saveIfAbsent(comics: List<Comic>): List<Long> {
        // Normal implementation: use existing save()
        return comics.map { comic -> saveIfAbsent(comic) }

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
    abstract fun get(comicId: Long): LiveData<Comic>

    @Query("SELECT * FROM Comic")
    abstract fun getAll(): LiveData<List<Comic>>

    @Query("SELECT ComicID FROM Comic WHERE FileUri = :fileUri")
    abstract fun getExistingId(fileUri: String): List<Long>

    fun existing(fileUri: String): Boolean {
        return getExistingId(fileUri).isEmpty()
    }

    @Query("SELECT Comic.* FROM Comic INNER JOIN Folder ON Comic.FolderID = Folder.FolderID WHERE FolderUri = :folderUri")
    abstract fun getComicsInFolder(folderUri: String): LiveData<List<Comic>>

    @Query("SELECT * FROM Comic WHERE FolderID = :folderId LIMIT 1")
    abstract fun getFirstComicInFolder(folderId: Long): Comic

    // Report bug when suitable:
    // This query is valid, but can't be parsed:
    // SELECT Comic.* FROM Comic INNER JOIN ComicFts Fts ON Comic.ComicID = Fts.docid WHERE Fts MATCH :name LIMIT :limit
    // Fts.docid is recognised, but not Fts MATCH
    @Query("SELECT Comic.ComicID, Comic.Title FROM Comic INNER JOIN ComicFts ON Comic.ComicID = ComicFts.docid WHERE ComicFts MATCH :name LIMIT :limit")
    abstract fun searchEverything(name: String, limit: Int): List<ComicMini>

    override fun search(name: String, limit: Int): List<ComicMini> {
        // TODO: convert all of these to single-expression function
        return searchEverything(name, limit)
    }

    @Query("DELETE FROM Comic")
    abstract fun truncate()

    companion object {
        const val LIST_SEPARATOR = ','
    }
}