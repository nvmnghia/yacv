package com.uet.nvmnghia.yacv.parser

import android.net.Uri
import android.os.Environment
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.utils.Constants
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
         * If nothing is given, use the depreciated way :).
         * TODO: remove this function.
         *
         * @param rootFolder Folder to scan for comic
         */
        fun scan(rootFolder: String? = Constants.DEFAULT_ROOT_FOLDER): Flow<Array<File?>> {
            // Param is val, i.e. no reassignment
            // https://stackoverflow.com/a/42540294/5959593
            val _folderPath =
                rootFolder ?: Environment.getExternalStorageDirectory().canonicalPath.toString()

            return flow {
                // Emit in chunk
                val BUFFER_SIZE = 10
                var buffer = arrayOfNulls<File>(BUFFER_SIZE)
                var counter = 0

                File(_folderPath).walkTopDown().forEach { file ->
                    if (isComic(file)) {
                        if (counter == BUFFER_SIZE) {
                            emit(buffer)
                            buffer = arrayOfNulls(BUFFER_SIZE)
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

        /**
         * Scan the given folder at [rootUri] for comics.
         */
        fun scan(rootUri: Uri): Flow<Array<File?>> {

        }
    }
}