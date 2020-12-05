package com.uet.nvmnghia.yacv.parser.metadata.generic

import com.uet.nvmnghia.yacv.model.comic.Comic
import java.io.File


class GenericMetadataParser {
    companion object {
        // TODO: Rewrite all metadata parsers so that when a Comic instance
        //  is created, the metadata is immediately parsed.
        fun parse(filePath: String): Comic {
            val file = File(filePath)
            val comic = Comic(filePath)
            comic.tmpSeries = file.name.substringBeforeLast('.')

            return comic
        }
    }
}