package com.uet.nvmnghia.yacv.model.folder

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


/**
 * Folder containing comic files.
 */
@Entity(indices = [Index(value = [Folder.COLUMN_FOLDER_URI], unique = true)])
data class Folder(
    @ColumnInfo(name = COLUMN_FOLDER_URI)
    val uri: String
) {

    constructor(folderUri: Uri) : this(folderUri.toString())

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "FolderID")
    var id: Long = 0

    companion object {
        internal const val COLUMN_FOLDER_URI = "FolderUri"
    }
}