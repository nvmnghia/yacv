package com.uet.nvmnghia.yacv.model.search

import android.os.Parcelable
import com.uet.nvmnghia.yacv.ui.search.SeeMorePlaceholder
import kotlinx.parcelize.Parcelize


/**
 * Mother of all queries.
 *
 * [query]: the query string itself
 * [preview]: preview query or not (see [MetadataSearchHandler.search]), default to false
 */
abstract class Query(
    open val query: String,
    open val preview: Boolean = false
) : Parcelable

/**
 * Wrapper for query of a single type/category/table.
 *
 * [type]: the type/category queried
 */
@Parcelize
data class QuerySingleType(
    override val query: String,
    val type: Int,
    override val preview: Boolean = false,
) : Query(query, preview)


/**
 * The same as [QuerySingleType], but for several types.
 *
 * [types]: the type/category queried
 */
@Parcelize
data class QueryMultipleTypes(
    override val query: String,
    val types: List<Int>,
    override val preview: Boolean = false,
) : Query(query, preview)


fun queryAllTypes(query: String, preview: Boolean = false): QueryMultipleTypes =
    QueryMultipleTypes(query, List(SORTED_METADATA_DAO.size) { it }, preview)

fun queryFromSeeMore(seeMore: SeeMorePlaceholder, preview: Boolean = false): QuerySingleType =
    QuerySingleType(seeMore.getLabel(), seeMore.getID().toInt(), preview)