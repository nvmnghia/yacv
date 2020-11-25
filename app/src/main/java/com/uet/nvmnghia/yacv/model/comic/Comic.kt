package com.uet.nvmnghia.yacv.model.comic

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Analogy
 * - Entity: data
 * - Dao: collection of queries
 * - Database: collection of collections of queries
 */

@Entity
data class Comic(var path: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    // Comic info
    var title: String? = null
    var summary: String? = null
    var year: Int? = null
    var genre: String? = null

    // File info
    @ColumnInfo(name = "current_page")
    var currentPage: Int = 0
    @ColumnInfo(name = "num_pages")
    var numPages: Int = 0
    @ColumnInfo(name = "file_type")
    var fileType: String? = null
}
