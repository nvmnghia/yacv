package com.uet.nvmnghia.yacv.parser

import android.content.Context
import android.os.Environment
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.*
import javax.inject.Inject


class ComicScanner @Inject constructor(val context: Context) {

    companion object {
        private val COMPRESSION_FORMATS = enumValues<ComicParser.ComicFileType>()
            .map { format -> format.extension }.toSet()

        private fun isComic(file: File): Boolean {
            if (!file.isFile) return false
            return COMPRESSION_FORMATS.contains(file.extension.toLowerCase(Locale.ROOT))
        }
    }

    /**
     * Scan the given folder.
     * If nothing is given, use the depreciated way :).
     *
     * @param folderPath Folder to scan for comic
     */
    fun scan(folderPath: String? = null): Flow<Array<File?>> {
        // Param is val, i.e. no reassignment
        // https://stackoverflow.com/a/42540294/5959593
        val _folderPath =
            folderPath ?: Environment.getExternalStorageDirectory().canonicalPath.toString()

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

}