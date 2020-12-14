package com.uet.nvmnghia.yacv.parser

import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.helper.walkTopDown
import com.uet.nvmnghia.yacv.utils.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.*


class ComicScanner {
    companion object {
        private val COMPRESSION_FORMATS = enumValues<ComicParser.ComicFileType>()
            .map { format -> format.extension }.toSet()

        private fun isComic(file: File): Boolean {
            if (!file.isFile) return false
            return file.extension.toLowerCase(Locale.ROOT) in COMPRESSION_FORMATS
        }

        private fun isComic(documentFile: DocumentFile): Boolean {
            return FileUtils.getExtension(documentFile) in COMPRESSION_FORMATS
        }

        /**
         * Scan the given folder for comics.
         * TODO: Implement deep scan.
         *
         * @param rootFolder Folder to scan for comics
         * @param deep Scan deeply, slower but guarantee to scan all files
         */
        fun scan(rootFolder: DocumentFile, deep: Boolean): Flow<Array<DocumentFile?>> {
            return flow {
                // Emit in chunk
                val BULK_SIZE = 10
                val IMMEDIATE_LIMIT = 10

                var buffer = arrayOfNulls<DocumentFile>(BULK_SIZE)
                var counter = 0
                var emitImmediate = true    // Emit immediately first 10 comics

                rootFolder.walkTopDown().forEach { document ->
                    if (isComic(document)) {
                        if (emitImmediate) {
                            if (counter < IMMEDIATE_LIMIT) {
                                emit(arrayOf(document) as Array<DocumentFile?>)    // Wtf?
                                counter++
                            } else {
                                emitImmediate = false
                                counter = 0
                            }
                        } else {
                            if (counter == BULK_SIZE) {
                                emit(buffer)
                                buffer = arrayOfNulls(BULK_SIZE)
                                counter = 0
                            }

                            buffer[counter] = document
                            counter++
                        }
                    }
                }

                // Emit the rest
                emit(buffer)
            }
        }
    }
}