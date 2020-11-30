package com.uet.nvmnghia.yacv.model.folder

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.File


/**
 * Folder containing comic files.
 */
@Entity(indices = [Index(value = ["path"], unique = true)])
data class Folder(val path: String) {

    constructor(folderPath: File) : this(folderPath.canonicalPath)

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}