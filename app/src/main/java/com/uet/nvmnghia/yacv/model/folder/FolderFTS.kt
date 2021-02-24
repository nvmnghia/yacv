package com.uet.nvmnghia.yacv.model.folder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4


@Entity
@Fts4(contentEntity = Folder::class)
class FolderFTS(
    @ColumnInfo(name = Folder.COLUMN_FOLDER_NAME)
    val name: String
)