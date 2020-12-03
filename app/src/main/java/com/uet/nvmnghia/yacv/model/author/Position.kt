package com.uet.nvmnghia.yacv.model.author

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Position enum and its table
 * https://softwareengineering.stackexchange.com/questions/305148/why-would-you-store-an-enum-in-db
 */


enum class Position(val id: Long) {
    Writer(1), Editor(2), Penciller(3),
    Inker(4), Colorist(5), Letterer(6),
    CoverArtist(7),
}


@Entity(tableName = "Position")
data class PositionTable(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "PositionID")
    val id: Int,
    @ColumnInfo(name = "Name")
    val name: String
) {
    companion object {
        fun populate(db: SupportSQLiteDatabase) {
            // There's a way, potentially a race condition,
            // that allows using Dao in onCreate():
            // https://gist.github.com/florina-muntenescu/697e543652b03d3d2a06703f5d6b44b5

            val positions = enumValues<Position>().joinToString(", ")
            { position -> "('${position.id}', '${position.name}')" }
            db.execSQL("INSERT INTO Position (PositionID, Name) VALUES $positions;")
        }
    }
}