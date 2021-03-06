package com.uet.nvmnghia.yacv.model.author

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uet.nvmnghia.yacv.model.search.METADATA_PRECEDENCE
import com.uet.nvmnghia.yacv.model.search.Metadata
import kotlinx.parcelize.Parcelize


/**
 * An author could have multiple roles:
 * writer/penciller/inker/colorist/letterer/editor.
 */
@Parcelize
@Entity
data class Author(
    @ColumnInfo(name = COLUMN_AUTHOR_NAME)
    val name: String,
) : Metadata {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_AUTHOR_ID)
    var id: Long = 0

    override fun getID() = id

    override fun getLabel() = name

    override fun getType(): Int = METADATA_TYPE

    companion object {
        const val COLUMN_AUTHOR_ID = "AuthorID"

        internal const val COLUMN_AUTHOR_NAME = "Name"

        val METADATA_TYPE: Int = METADATA_PRECEDENCE[Author::class]!!
    }

}