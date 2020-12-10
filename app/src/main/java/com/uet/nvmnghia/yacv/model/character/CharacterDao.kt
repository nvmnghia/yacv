package com.uet.nvmnghia.yacv.model.character

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
abstract class CharacterDao {
    /**
     * Save without checking duplicate.
     * Only suitable for internal use.
     */
    @Insert
    protected abstract fun saveUnsafe(character: Character): Long

    /**
     * Same as the overloaded method.
     */
    @Insert
    protected abstract fun saveUnsafe(characters: List<Character>): List<Long>

    /**
     * Save with checking duplicate.
     */
    @Transaction
    open fun saveIfAbsent(name: String): Long {
        val trimmedName = name.trim()
        val id = searchIdByName(trimmedName)
        return if (id.isNotEmpty()) {
            id[0]
        } else {
            saveUnsafe(Character(trimmedName))
        }
    }

    /**
     * Same as the overloaded method.
     */
    @Transaction
    open fun saveIfAbsent(names: Iterable<String>): List<Long> {
        return names.map { name -> saveIfAbsent(name) }
    }

    /**
     * Deduplicate, then save.
     * Returns a [HashMap] that maps a character name to its ID.
     */
    fun dedupThenSaveIfAbsent(names: Iterable<String>): HashMap<String, Long> {
        val nameSet = names.toSet()
        val characterIds = saveIfAbsent(nameSet)

        var counter = 0
        val mapCharacterToId = HashMap<String, Long>()
        nameSet.forEach { name -> mapCharacterToId[name] = characterIds[counter++] }

        return mapCharacterToId
    }

    @Query("SELECT * FROM Character WHERE rowid = :characterId")
    abstract fun get(characterId: Long): LiveData<Character>

    @Query("SELECT * FROM Character")
    abstract fun getAll(): LiveData<List<Character>>

    @Query("SELECT docid FROM CharacterFts WHERE Name MATCH :name")
    abstract fun searchIdByName(name: String): List<Long>

    @Query("DELETE FROM Character")
    abstract fun truncate()

//    @Transaction    // There're actually 2 queries in @Relation-related query
//    @Query("SELECT * FROM Character WHERE rowid = :characterId")
//    abstract fun getCharacterWithComic(characterId: Long): CharacterWithComics
}