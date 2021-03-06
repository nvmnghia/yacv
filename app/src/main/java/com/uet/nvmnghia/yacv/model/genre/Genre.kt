package com.uet.nvmnghia.yacv.model.genre

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uet.nvmnghia.yacv.model.search.METADATA_PRECEDENCE
import com.uet.nvmnghia.yacv.model.search.Metadata
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity
data class Genre(
    @ColumnInfo(name = "Name")
    val name: String
) : Metadata {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_GENRE_ID)
    var id: Long = 0

    override fun getID() = id

    override fun getLabel() = name

    override fun getType(): Int = METADATA_TYPE

    companion object {
        const val COLUMN_GENRE_ID = "GenreID"

        internal const val COLUMN_GENRE_NAME = "Name"

        val METADATA_TYPE: Int = METADATA_PRECEDENCE[Genre::class]!!
    }

}