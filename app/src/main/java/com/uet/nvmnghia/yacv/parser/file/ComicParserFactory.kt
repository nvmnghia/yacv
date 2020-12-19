package com.uet.nvmnghia.yacv.parser.file

import android.content.Context
import android.net.Uri
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
        fun create(context: Context, document: DocumentFile?): ComicParser? {
            if (document == null) {
                return null
            }

            return when (FileUtils.getExtension(document)) {
                ComicParser.ComicFileType.CBZ.extension -> CBZParser(context, document)
                else -> return null
            }
        }

        fun create(context: Context, uri: String): ComicParser? {
            return create(context, DocumentFile.fromSingleUri(context, Uri.parse(uri)))
        }
    }

}