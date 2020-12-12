package com.uet.nvmnghia.yacv.model.join

import androidx.room.*
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.Comic


@Entity(
    primaryKeys = [Comic.COLUMN_COMIC_ID, Character.COLUMN_CHARACTER_ID],
    indices = [Index(value = [Character.COLUMN_CHARACTER_ID])],
    foreignKeys = [
        ForeignKey(entity = Comic::class,
            parentColumns = [Comic.COLUMN_COMIC_ID],
            childColumns  = [Comic.COLUMN_COMIC_ID]),
        ForeignKey(entity = Character::class,
            parentColumns = [Character.COLUMN_CHARACTER_ID],
            childColumns  = [Character.COLUMN_CHARACTER_ID]),
    ]
)
data class ComicCharacterJoin(
    @ColumnInfo(name = Comic.COLUMN_COMIC_ID)
    val comicId: Long,
    @ColumnInfo(name = Character.COLUMN_CHARACTER_ID)
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
}

// Currently CrossRef is not flexible enough, so let's juts JOIN manually
//data class CharacterWithComics (
//    @Embedded val character: Character,
//    @Relation(
//        parentColumn = Character.COLUMN_CHARACTER_ID,
//        entityColumn = Comic.COLUMN_COMIC_ID,
//        associateBy = Junction(CharacterComicCrossRef::class)
//    )
//    val comics: List<Comic>
//)
//
//data class ComicWithCharacters (
//    @Embedded val comic: Comic,
//    @Relation(
//        parentColumn = Comic.COLUMN_COMIC_ID,
//        entityColumn = Character.COLUMN_CHARACTER_ID,
//        associateBy = Junction(CharacterComicCrossRef::class)
//    )
//    val characters: List<Character>
//)