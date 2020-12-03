package com.uet.nvmnghia.yacv.model.character

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Character::class)
data class CharacterFts(
    @ColumnInfo(name = "Name")
    val name: String
)