package com.uet.nvmnghia.yacv.model.comic

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


/**
 * Fts entity for [Comic], storing Fts fields only.
 */
@Entity
@Fts4(contentEntity = Comic::class)
data class ComicFts(
    // @formatter:off
    @ColumnInfo(name = Comic.COLUMN_TITLE)
    var title    : String?,
    @ColumnInfo(name = Comic.COLUMN_SUMMARY)
    var summary  : String?,
    @ColumnInfo(name = Comic.COLUMN_PUBLISHER)
    var publisher: String?,
    @ColumnInfo(name = Comic.COLUMN_WEB)
    var web      : String?,
    // @formatter:on
)