package com.uet.nvmnghia.yacv.parser.metadata

import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.metadata.comicrack.ComicRackParser
import java.io.InputStream


class MetadataParser {

    companion object {
        /**
         * List of available parsers.
         */
        private val PARSERS = arrayOf<INonGenericMetadataParser>(ComicRackParser())

        /**
         * Given a file name, check if the file is parsable by any parser.
         */
        fun checkParsableByName(metadataFilename: String): Boolean {
            for (parser in PARSERS) {
                if (parser.checkParsableByName(metadataFilename)) {
                    return true
                }
            }

            return false
        }

        /**
         * Given a metadata input stream [mis], its name, and a [Comic] instance [comic],
         * parse metadata into [comic].
         */
        fun parse(mis: InputStream?, metadataFilename: String?, comic: Comic) {
            GenericMetadataParser.parse(comic)

            mis?.let {
                for (parser in PARSERS) {
                    if (parser.checkParsableByName(metadataFilename)) {
                        parser.parse(it, comic)
                        break
                    }
                }
            }
        }
    }

}