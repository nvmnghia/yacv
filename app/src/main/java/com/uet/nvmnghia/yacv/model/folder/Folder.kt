package com.uet.nvmnghia.yacv.model.folder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.uet.nvmnghia.yacv.model.search.METADATA_PRECEDENCE
import com.uet.nvmnghia.yacv.model.search.Metadata
import kotlinx.parcelize.Parcelize


/**
 * Folder containing comic files.
 */
@Parcelize
@Entity(indices = [Index(value = [Folder.COLUMN_FOLDER_URI], unique = true)])
data class Folder(
    @ColumnInfo(name = COLUMN_FOLDER_URI)
    val uri: String,
    @ColumnInfo(name = COLUMN_FOLDER_NAME)
    val name: String,
) : Metadata {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_FOLDER_ID)
    var id: Long = 0

    override fun getID(): Long = id

    override fun getLabel(): String = name

    override fun getType(): Int = METADATA_TYPE

    companion object {
        const val COLUMN_FOLDER_ID = "FolderID"
        const val COLUMN_FOLDER_URI = "FolderUri"
        const val COLUMN_FOLDER_NAME = "Name"

        val METADATA_TYPE: Int = METADATA_PRECEDENCE[Folder::class]!!
    }

}