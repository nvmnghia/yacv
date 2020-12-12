package com.uet.nvmnghia.yacv.model.folder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.File


/**
 * Folder containing comic files.
 */
@Entity(indices = [Index(value = [Folder.COLUMN_FOLDER_PATH], unique = true)])
data class Folder(
    @ColumnInfo(name = COLUMN_FOLDER_PATH)
    val uri: String
) {
    constructor(folderPath: File) : this(folderPath.canonicalPath)

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_FOLDER_ID)
    var id: Long = 0

    companion object {
        const val COLUMN_FOLDER_ID  = "FolderID"
        const val COLUMN_FOLDER_PATH = "FolderPath"
    }
}