package com.uet.nvmnghia.yacv.parser.file

import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.parser.file.impl.CBZParser
import com.uet.nvmnghia.yacv.utils.FileUtils


class ComicParserFactory {

    companion object {
//        /**
//         * Given a file, create a parser instance for that file.
//         */
//        fun create(file: File): ComicParser {
//            return when (file.extension) {
//                ComicParser.ComicFileType.CBZ.extension -> CBZParser(filePath = file.canonicalPath)
//                else -> throw IllegalArgumentException("Cannot create ComicParser from ${file.canonicalPath}")
//            }
//        }
//
//        /**
//         * Same as the overloaded method.
//         */
//        fun create(filePath: String): ComicParser {
//            return create(File(filePath))
//        }

        /**
         * Given a file, create a parser instance for that file.
         */
        fun create(document: DocumentFile): ComicParser? {
            return when (FileUtils.getExtension(document)) {
                ComicParser.ComicFileType.CBZ.extension -> CBZParser(document)
                else -> return null
            }
        }
    }

}