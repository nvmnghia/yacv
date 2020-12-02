package com.uet.nvmnghia.yacv.model.join

import androidx.room.*


@Entity(primaryKeys = ["CharacterID", "ComicID"])
data class CharacterComicJoin(
    @ColumnInfo(name = "CharacterID")
    val characterId: Long,
    @ColumnInfo(name = "ComicID")
    val comicId: Long,
)


@Dao
interface CharacterComicJoinDao {
    @Insert
    fun save(join: CharacterComicJoin)

    fun save(comicId: Long, characterId: Long) {
        save(CharacterComicJoin(characterId, comicId))
    }

    @Transaction
    fun save(comicId: Long, characterIds: Iterable<Long>) {
        characterIds.toSet()    // Avoid duplication. For example an erroneous <Characters>: "batMan, batman"
            .forEach { characterId -> save(CharacterComicJoin(characterId, comicId)) }
    }
}

// Currently CrossRef is not flexible enough, so let's juts JOIN manually
//data class CharacterWithComics (
//    @Embedded val character: Character,
//    @Relation(
//        parentColumn = "CharacterID",
//        entityColumn = "ComicID",
//        associateBy = Junction(CharacterComicCrossRef::class)
//    )
//    val comics: List<Comic>
//)
//
//data class ComicWithCharacters (
//    @Embedded val comic: Comic,
//    @Relation(
//        parentColumn = "ComicID",
//        entityColumn = "CharacterID",
//        associateBy = Junction(CharacterComicCrossRef::class)
//    )
//    val characters: List<Character>
//)