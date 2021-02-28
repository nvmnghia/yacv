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
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


//================================================================================
// Placeholders
//================================================================================

/**
 * Class for placeholders: group title and See More.
 * The ID of the placeholders is the group ID of the corresponding group,
 * i.e. METADATA_TYPE of the other [Metadata] classes.
 */
abstract class MetadataPlaceholder(protected open var id: Long) : Metadata {

    override fun getID(): Long = id

    abstract override fun getLabel(): String

    abstract override fun getType(): Int

}


/**
 * Label of result group is the group title.
 */
@Parcelize
class ResultGroupHeaderPlaceholder(override var id: Long) : MetadataPlaceholder(id) {

    constructor(sample: Metadata) : this(sample.getType().toLong())

    @IgnoredOnParcel
    private var title: String = MAP_METADATA_TYPE_2_TITLE[id.toInt()]
        ?: throw IllegalStateException("Unexpected metadata of ID $id")

    override fun getLabel(): String = title

    override fun getType(): Int = METADATA_TYPE

    companion object {
        const val METADATA_TYPE: Int = -1
    }

}


/**
 * Label of See More is the query string.
 */
@Parcelize
class SeeMorePlaceholder(override var id: Long, private val query: String) : MetadataPlaceholder(id) {

    constructor(sample: Metadata, query: String) : this(sample.getID(), query)

    override fun getLabel(): String = query

    override fun getType(): Int = METADATA_TYPE

    companion object {
        const val METADATA_TYPE = -2
    }

}


//================================================================================
// KClass to ID to title
//================================================================================

// Map class to group title
lateinit var MAP_METADATA_TYPE_2_TITLE: Map<Int, String>


/**
 * Initialize [MAP_METADATA_TYPE_2_TITLE]. Should be called in MainApplication
 */
fun initializeMetadataTitle(context: Context) {
    // If initialized, returns
    if (::MAP_METADATA_TYPE_2_TITLE.isInitialized) return

    MAP_METADATA_TYPE_2_TITLE = METADATA_PRECEDENCE.entries.associate { (kclass, groupID) ->
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
