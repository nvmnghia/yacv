package com.uet.nvmnghia.yacv.model.author

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey


/**
 * An author could be a writer/penciller/inker/colorist/letterer/editor.
 */
@Entity
data class Author(
    @ColumnInfo(name = "Name")
    val name: String,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "AuthorID")
    var id: Long = 0
}