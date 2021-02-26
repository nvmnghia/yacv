package com.uet.nvmnghia.yacv.model.search

import android.os.Parcelable
import com.uet.nvmnghia.yacv.ui.search.SeeMorePlaceholder
import kotlinx.parcelize.Parcelize


/**
 * Mother of all queries.
 */
interface Query : Parcelable

/**
 * Wrapper for query of a single type/category/table.
 *
 * [query]: the query string itself
 * [type]: the type/category queried
 * [preview]: preview query or not (see [MetadataSearchHandler.search]), default to false
 */
@Parcelize
data class QuerySingleType(
    val query: String,
    val type: Int,
    val preview: Boolean = false,
) : Parcelable


/**
 * The same as [QuerySingleType], but:
 * - for several types, and
 * - [preview] default to true.
 */
@Parcelize
data class QueryMultipleTypes(
    val query: String,
    val types: List<Int>,
    val preview: Boolean = false,
) : Parcelable


fun queryAllTypes(query: String, preview: Boolean = false): QueryMultipleTypes =
    QueryMultipleTypes(query, List(SORTED_METADATA_DAO.size) { it }, preview)

fun queryFromSeeMore(seeMore: SeeMorePlaceholder, preview: Boolean = false): QuerySingleType =
    QuerySingleType(seeMore.getLabel(), seeMore.getID().toInt(), preview)