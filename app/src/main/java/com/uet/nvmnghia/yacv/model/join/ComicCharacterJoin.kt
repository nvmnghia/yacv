package com.uet.nvmnghia.yacv.model.join

import androidx.room.*
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.Comic


@Entity(
    primaryKeys = ["ComicID", "CharacterID"],
    indices = [Index(value = ["CharacterID"])],
    foreignKeys = [
        ForeignKey(entity = Comic::class,
            parentColumns = ["ComicID"],
            childColumns = ["ComicID"]),
        ForeignKey(entity = Character::class,
            parentColumns = ["CharacterID"],
            childColumns = ["CharacterID"]),
    ]
)
data class ComicCharacterJoin(
    @ColumnInfo(name = "ComicID")
    val comicId: Long,
    @ColumnInfo(name = "CharacterID")
    val characterId: Long,
)


@Dao
interface ComicCharacterJoinDao {
    @Insert
    fun save(join: ComicCharacterJoin)

    fun save(comicId: Long, characterId: Long) {
        save(ComicCharacterJoin(comicId, characterId))
    }

    @Transaction
    fun save(comicId: Long, characterIds: Iterable<Long>) {
        characterIds.toSet()    // Avoid duplication. For example an erroneous <Characters>: "batMan, batman"
            .forEach { characterId -> save(ComicCharacterJoin(comicId, characterId)) }
    }

    @Query("DELETE FROM ComicCharacterJoin")
    fun truncate()
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