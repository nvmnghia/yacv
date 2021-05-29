package com.uet.nvmnghia.yacv.parser.metadata

import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.metadata.comicrack.ComicRackParser
import java.io.InputStream
import java.util.*


class MetadataParser {

    companion object {
        /**
         * List of available parsers.
         */
        private val PARSERS = arrayOf<INonGenericMetadataParser>(ComicRackParser())

        /**
         * Given a file name, check if the file is parsable by any parser.
         */
        fun isParsableByName(metadataFilename: String?): Boolean {
            if (metadataFilename == null) {
                return false
            }

            val metadataFilenameLowercase = metadataFilename.toLowerCase(Locale.ROOT)
            for (parser in PARSERS) {
                if (parser.isParsableByName(metadataFilenameLowercase, true)) {
                    return true
                }
            }

            return false
        }

        /**
         * Given a metadata input stream [mis], its name, and a [Comic] instance [comic],
         * parse metadata into [comic].
         */
        fun parseByFilename(comic: Comic, metadataFilename: String, mis: InputStream) {
            GenericMetadataParser.parse(comic)

            val metadataFilenameLowercase = metadataFilename.toLowerCase(Locale.ROOT)
            mis.let {
                for (parser in PARSERS) {
                    if (parser.isParsableByName(metadataFilenameLowercase, true)) {
                        parser.parse(it, comic)
                        break
                    }
                }
            }
        }

        /**
         * Given the content of a metadata file, parse metadata into [comic].
         * Some metadata is stored as a string in comment section, and it is handled here.
         */
        fun parseByContent(metadataContent: String, comic: Comic) {
            TODO("To be implemented")
        }
    }

}