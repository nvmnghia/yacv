package com.uet.nvmnghia.yacv.model.comic

import androidx.room.ColumnInfo
import com.uet.nvmnghia.yacv.model.search.METADATA_PRECEDENCE
import com.uet.nvmnghia.yacv.model.search.Metadata
import kotlinx.parcelize.Parcelize


@Parcelize
class ComicMini(
    @ColumnInfo(name = Comic.COLUMN_COMIC_ID)
    var id: Long = 0,
    @ColumnInfo(name = Comic.COLUMN_TITLE)
    var title: String,
    @ColumnInfo(name = Comic.COLUMN_COMIC_URI)
    val fileUri: String,
) : Metadata {

    override fun getID(): Long = id

    override fun getLabel(): String = title

    override fun getType(): Int = METADATA_TYPE

    companion object {
        val METADATA_TYPE: Int = METADATA_PRECEDENCE[ComicMini::class]!!
    }
}