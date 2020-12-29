package com.uet.nvmnghia.yacv.parser.file

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.parser.file.impl.CBZParser
import com.uet.nvmnghia.yacv.utils.FileUtils


class ComicParserFactory {

    companion object {

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
            return create(context, Uri.parse(uri))
        }

        fun create(context: Context, uri: Uri): ComicParser? {
            return create(context, DocumentFile.fromSingleUri(context, uri))
        }
    }

}