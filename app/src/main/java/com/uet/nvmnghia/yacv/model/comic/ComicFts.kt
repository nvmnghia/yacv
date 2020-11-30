package com.uet.nvmnghia.yacv.model.comic

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
    val series    : String?  ,
    val writer    : String?  ,
    val title     : String?  ,
    val genre     : String?  ,
    val summary   : String?  ,
    val characters: String?  ,
    val language  : String?  ,
    val publisher : String?  ,
    val bw        : Boolean? ,
    val manga     : Boolean? ,
    val date      : Calendar?,
    val web       : String?  ,
    // @formatter:on
)