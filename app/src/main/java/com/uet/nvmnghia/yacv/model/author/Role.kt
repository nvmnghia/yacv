package com.uet.nvmnghia.yacv.model.author

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Role enum and its table
 * https://softwareengineering.stackexchange.com/questions/305148/why-would-you-store-an-enum-in-db
 */


enum class Role(val id: Long) {
    Writer(1), Editor(2), Penciller(3),
    Inker(4), Colorist(5), Letterer(6),
    CoverArtist(7),
}


@Entity(tableName = "Role")
data class RoleTable(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_ROLE_ID)
    val id: Int,
    @ColumnInfo(name = "Name")
    val name: String
) {
    companion object {
        const val COLUMN_ROLE_ID = "RoleID"

        fun populate(db: SupportSQLiteDatabase) {
            // There's a way, potentially a race condition,
            // that allows using Dao in onCreate():
            // https://gist.github.com/florina-muntenescu/697e543652b03d3d2a06703f5d6b44b5

            val roles = enumValues<Role>().joinToString(", ")
                { role -> "('${role.id}', '${role.name}')" }
            db.execSQL("INSERT INTO Role (RoleID, Name) VALUES $roles;")
        }
    }
}