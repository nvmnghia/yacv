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

/**
 * Class for placeholders: group title and See More.
 * The ID of the placeholders is the group ID of the corresponding group,
 * i.e. METADATA_GROUP_ID of the other [SearchableMetadata] classes.
 */
abstract class MetadataPlaceholder(sample: SearchableMetadata) : SearchableMetadata {

    protected var id: Long

    init {
        val kclass = sample::class
        id = MAP_CLASS_2_GROUP_ID[kclass]?.toLong()
            ?: throw IllegalStateException("Unexpected metadata of type $kclass")
    }

    override fun getID(): Long = id

    abstract override fun getLabel(): String

    abstract override fun getGroupID(): Int

}


class ResultGroupPlaceholder(sample: SearchableMetadata) : MetadataPlaceholder(sample) {

    private var title: String = MAP_GROUP_ID_2_TITLE[id.toInt()]
        ?: throw IllegalStateException("Unexpected metadata of ID $id")

    override fun getLabel(): String = title

    override fun getGroupID(): Int = METADATA_GROUP_ID

    companion object {
        const val METADATA_GROUP_ID: Int = -1
    }

}


class SeeMorePlaceholder(sample: SearchableMetadata) : MetadataPlaceholder(sample) {

    override fun getLabel(): String = "SeeMore"

    override fun getGroupID(): Int = METADATA_GROUP_ID

    companion object {
        const val METADATA_GROUP_ID = -2
    }

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

    MAP_GROUP_ID_2_TITLE = METADATA_PRECEDENCE.entries.associate { (kclass, groupID) ->
        groupID to when (kclass) {
            ComicMini::class -> context.resources.getString(R.string.comic)
            Series::class    -> context.resources.getString(R.string.series)
            Character::class -> context.resources.getString(R.string.character)
            Author::class    -> context.resources.getString(R.string.author)
            Genre::class     -> context.resources.getString(R.string.genre)
            else -> throw IllegalStateException("Unexpected metadata of type $kclass")
        }
    }
}
