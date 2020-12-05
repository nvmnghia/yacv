package com.uet.nvmnghia.yacv.parser.file

import com.uet.nvmnghia.yacv.parser.file.impl.CBZParser
import java.io.File


class ComicParserFactory {

    companion object {
        /**
         * Given a file, create a parser instance for that file.
         */
        fun create(file: File): ComicParser? {
            return when (file.extension) {
                ComicParser.ComicFileType.CBZ.extension -> CBZParser(filePath = file.canonicalPath)
                else -> null
            }
        }

        /**
         * Same as the overloaded method.
         */
        fun create(filePath: String): ComicParser? {
            return create(File(filePath))
        }
    }

}