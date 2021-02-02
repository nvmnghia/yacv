package com.uet.nvmnghia.yacv.model.folder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.uet.nvmnghia.yacv.model.search.SearchableMetadata


/**
 * Folder containing comic files.
 */
@Entity(indices = [Index(value = [Folder.COLUMN_FOLDER_URI], unique = true)])
data class Folder(
    @ColumnInfo(name = COLUMN_FOLDER_URI)
    val uri: String,
    @ColumnInfo(name = "Name")
    val name: String,
) : SearchableMetadata {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_FOLDER_ID)
    var id: Long = 0

    override fun getID() = id

    override fun getLabel() = name

    companion object {
        const val COLUMN_FOLDER_ID  = "FolderID"
        const val COLUMN_FOLDER_URI = "FolderUri"
    }

}