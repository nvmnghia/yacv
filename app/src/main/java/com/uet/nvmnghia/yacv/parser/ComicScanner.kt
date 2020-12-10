package com.uet.nvmnghia.yacv.parser

import com.uet.nvmnghia.yacv.parser.file.ComicParser
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
            return COMPRESSION_FORMATS.contains(file.extension.toLowerCase(Locale.ROOT))
        }

        /**
         * Scan the given folder for comics.
         * TODO: Implement deep scan.
         *
         * @param rootFolder Folder to scan for comics
         * @param deep Scan deeply, slower but guarantee to scan all files
         */
        fun scan(rootFolder: String, deep: Boolean): Flow<Array<File?>> {
            return flow {
                // Emit in chunk
                val BULK_SIZE = 10
                var buffer = arrayOfNulls<File>(BULK_SIZE)
                var counter = 0

                // TODO: Emit early: emit every BULK_SIZE or 0.5 second
                File(rootFolder).walkTopDown().forEach { file ->
                    if (isComic(file)) {
                        if (counter == BULK_SIZE) {
                            emit(buffer)
                            buffer = arrayOfNulls(BULK_SIZE)
                            counter = 0
                        }

                        buffer[counter] = file
                        counter++
                    }
                }

                // Emit the rest
                emit(buffer)
            }
        }
    }
}