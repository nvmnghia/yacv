package com.uet.nvmnghia.yacv.model.character

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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

    companion object {
        const val COLUMN_CHARACTER_ID = "CharacterID"

        internal const val COLUMN_CHARACTER_NAME = "Name"
    }

}