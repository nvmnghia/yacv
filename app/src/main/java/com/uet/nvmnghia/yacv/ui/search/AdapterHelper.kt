package com.uet.nvmnghia.yacv.ui.search

import android.content.Context
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.search.METADATA_PRECEDENCE
import com.uet.nvmnghia.yacv.model.search.SearchableMetadata
import com.uet.nvmnghia.yacv.model.series.Series


//================================================================================
// Placeholders
//================================================================================

class ResultGroupPlaceholder(sample: SearchableMetadata) : SearchableMetadata {

    private var title: String
    private var id: Long

    init {
        val kclass = sample::class
        id    = MAP_CLASS_2_GROUP_ID[kclass]?.toLong()
            ?: throw IllegalStateException("Unexpected metadata of type $kclass")
        title = MAP_GROUP_ID_2_TITLE[id.toInt()]
            ?: throw IllegalStateException("Unexpected metadata of type $kclass")
    }

    override fun getID(): Long = id

    override fun getLabel(): String = title

    override fun getType(): Int = METADATA_GROUP_ID

    companion object {
        const val METADATA_GROUP_ID: Int = -1
    }

}


val SeeMorePlaceholder = object : SearchableMetadata {
    override fun getID(): Long = -2

    override fun getLabel(): String = "SeeMore"    // Dummy, the label is read from resource

    override fun getType(): Int = getID().toInt()
}


//================================================================================
// KClass to ID to title
//================================================================================

// Map class to group ID
val MAP_CLASS_2_GROUP_ID = METADATA_PRECEDENCE

// Map class to group title
lateinit var MAP_GROUP_ID_2_TITLE: Map<Int, String>


/**
 * Initialize [MAP_GROUP_ID_2_TITLE]. Should be called in MainApplication
 */
fun initialize(context: Context) {
    // If initialized, returns
    if (::MAP_GROUP_ID_2_TITLE.isInitialized) return

    // If not initialized, lock for one initialization.
    // This SHOULD not be needed.
//            synchronized(MAP_METADATA_TITLE) {
//                if (this::MAP_METADATA_TITLE.isInitialized) return
//            }

    MAP_GROUP_ID_2_TITLE = METADATA_PRECEDENCE.entries.associate { (kclass, groupID) ->
        groupID to when (kclass) {
            ComicMini::class -> context.resources.getString(R.string.comic)
            Series::class -> context.resources.getString(R.string.series)
            Character::class -> context.resources.getString(R.string.character)
            Author::class -> context.resources.getString(R.string.author)
            Genre::class -> context.resources.getString(R.string.genre)
            else -> throw IllegalStateException("Unexpected metadata of type $kclass")
        }
    }
}
