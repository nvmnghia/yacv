package com.uet.nvmnghia.yacv.model.character

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.search.METADATA_PRECEDENCE
import com.uet.nvmnghia.yacv.model.search.SearchableMetadata


/**
 * A comic character.
 */
@Entity
data class Character(
    @ColumnInfo(name = COLUMN_CHARACTER_NAME)
    val name: String
) : SearchableMetadata {

    // ID could be omitted for FTS, but if present:
    // - Type: must be Int
    // - Row name: must be "rowid"
    // - SELECT: explicitly mention "rowid"
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_CHARACTER_ID)
    var id: Long = 0

    override fun getID() = id

    override fun getLabel() = name

    override fun getGroupID(): Int = METADATA_GROUP_ID

    companion object {
        const val COLUMN_CHARACTER_ID = "CharacterID"

        internal const val COLUMN_CHARACTER_NAME = "Name"

        val METADATA_GROUP_ID: Int = METADATA_PRECEDENCE[Character::class]!!
    }

}