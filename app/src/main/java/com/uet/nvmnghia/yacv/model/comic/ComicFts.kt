package com.uet.nvmnghia.yacv.model.comic

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import java.util.*


/**
 * Fts entity for [Comic], storing Fts fields only.
 */
@Entity
@Fts4(contentEntity = Comic::class)
data class ComicFts(
    // @formatter:off
    @ColumnInfo(name = "Series")
    var series   : String?,
    @ColumnInfo(name = "Writer")
    var writer   : String?,
    @ColumnInfo(name = "Title")
    var title    : String?,
    @ColumnInfo(name = "Summary")
    var summary  : String?,
    @ColumnInfo(name = "Language")
    var language : String?,
    @ColumnInfo(name = "Publisher")
    var publisher: String?,
    @ColumnInfo(name = "Web")
    var web      : String?,
    // @formatter:on
)