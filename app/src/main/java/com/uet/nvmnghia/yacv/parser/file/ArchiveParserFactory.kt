package com.uet.nvmnghia.yacv.parser.file

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.parser.file.impl.CBZParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import java.lang.IllegalStateException


internal class ArchiveParserFactory {
    companion object {

        /**
         * Given a [uri], return a suitable [ArchiveParser].
         */
        fun createArchiveParser(context: Context, uri: Uri): ArchiveParser {
            return when (FileUtils.getExtension(uri)) {
                ComicParser.ComicFileType.CBZ.extension -> CBZParser(context, uri)
                else -> throw IllegalStateException("Unsupported archive format for document with URI: $uri")
            }
        }

        fun createArchiveParser(context: Context, document: DocumentFile): ArchiveParser {
            return createArchiveParser(context, document.uri)
        }

    }
}