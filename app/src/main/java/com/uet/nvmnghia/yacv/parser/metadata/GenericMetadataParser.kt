package com.uet.nvmnghia.yacv.parser.metadata

import android.net.Uri
import com.uet.nvmnghia.yacv.model.comic.Comic


/**
 * Given a [Comic] instance, try to parse anything from its file name.
 */
class GenericMetadataParser {
    companion object {
        /**
         * Given a [Comic] instance, try to parse anything from its file name.
         */
        fun parse(comic: Comic) {
            // Stupid rule: Comic file name is likely to be the title.
            comic.tmpSeries = Uri.parse(comic.fileUri)
                .lastPathSegment
                ?.substringBeforeLast('.')
        }
    }
}