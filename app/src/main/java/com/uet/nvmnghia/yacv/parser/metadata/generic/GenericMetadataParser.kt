package com.uet.nvmnghia.yacv.parser.metadata.generic

import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.model.comic.Comic


class GenericMetadataParser {
    companion object {
        // TODO: Rewrite all metadata parsers so that when a Comic instance
        //  is created, the metadata is immediately parsed.
        // TODO: Check parent folder. If parent name is "parsable", then it is possibly the series.
        fun parse(document: DocumentFile): Comic {
            val comic = Comic(document)
            comic.tmpSeries = document.name?.substringBeforeLast('.')

            return comic
        }
    }
}