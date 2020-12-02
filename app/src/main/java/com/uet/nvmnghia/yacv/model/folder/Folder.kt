package com.uet.nvmnghia.yacv.model.folder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.File


/**
 * Folder containing comic files.
 */
@Entity(indices = [Index(value = ["FolderPath"], unique = true)])
data class Folder(
    @ColumnInfo(name = "FolderPath")
    val path: String
) {

    constructor(folderPath: File) : this(folderPath.canonicalPath)

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "FolderID")
    var id: Long = 0
}