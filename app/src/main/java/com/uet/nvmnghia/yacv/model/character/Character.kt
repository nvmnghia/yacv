package com.uet.nvmnghia.yacv.model.character

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey


/**
 * A comic character.
 */
@Entity
data class Character(
    @ColumnInfo(name = "Name")
    val name: String
) {
    // ID could be omitted for FTS, but if present:
    // - Type: must be Int
    // - Row name: must be "rowid"
    // - SELECT: explicitly mention "rowid"
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CharacterID")
    var id: Long = 0
}