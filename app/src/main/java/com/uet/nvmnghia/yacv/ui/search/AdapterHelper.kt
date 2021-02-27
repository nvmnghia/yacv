package com.uet.nvmnghia.yacv.ui.search

import android.content.Context
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.author.Author
import com.uet.nvmnghia.yacv.model.character.Character
import com.uet.nvmnghia.yacv.model.comic.ComicMini
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.model.genre.Genre
import com.uet.nvmnghia.yacv.model.search.METADATA_PRECEDENCE
import com.uet.nvmnghia.yacv.model.search.Metadata
import com.uet.nvmnghia.yacv.model.series.Series


//================================================================================
// Placeholders
//================================================================================

/**
 * Class for placeholders: group title and See More.
 * The ID of the placeholders is the group ID of the corresponding group,
 * i.e. METADATA_GROUP_ID of the other [Metadata] classes.
 */
abstract class MetadataPlaceholder(sample: Metadata) :
    Metadata {

    protected var id: Long

    init {
        val kclass = sample::class
        id = MAP_CLASS_2_GROUP_ID[kclass]?.toLong()
            ?: throw IllegalStateException("Unexpected metadata of type $kclass")
    }

    override fun getID(): Long = id

    abstract override fun getLabel(): String

    abstract override fun getType(): Int

}


/**
 * Label of result group is the group title.
 */
class ResultGroupPlaceholder(sample: Metadata) : MetadataPlaceholder(sample) {

    private var title: String = MAP_GROUP_ID_2_TITLE[id.toInt()]
        ?: throw IllegalStateException("Unexpected metadata of ID $id")

    override fun getLabel(): String = title

    override fun getType(): Int = METADATA_GROUP_ID

    companion object {
        const val METADATA_GROUP_ID: Int = -1
    }

}


/**
 * Label of See More is the query string.
 */
class SeeMorePlaceholder(sample: Metadata, private val query: String) : MetadataPlaceholder(sample) {

    override fun getLabel(): String = query

    override fun getType(): Int = METADATA_GROUP_ID

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
fun initializeMetadataTitle(context: Context) {
    // If initialized, returns
    if (::MAP_GROUP_ID_2_TITLE.isInitialized) return

    MAP_GROUP_ID_2_TITLE = METADATA_PRECEDENCE.entries.associate { (kclass, groupID) ->
        groupID to when (kclass) {
            ComicMini::class -> context.resources.getString(R.string.comic)
            Series::class    -> context.resources.getString(R.string.series)
            Folder::class    -> context.resources.getString(R.string.folder)
            Character::class -> context.resources.getString(R.string.character)
            Author::class    -> context.resources.getString(R.string.author)
            Genre::class     -> context.resources.getString(R.string.genre)
            else -> throw IllegalStateException("Unexpected metadata of type $kclass")
        }
    }
}
