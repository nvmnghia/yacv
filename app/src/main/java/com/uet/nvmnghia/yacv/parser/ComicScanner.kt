package com.uet.nvmnghia.yacv.parser

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.helper.walkTopDown
import com.uet.nvmnghia.yacv.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject


class ComicScanner @Inject constructor(val context: Context) {

    companion object {
        private val COMPRESSION_FORMATS = enumValues<ComicParser.ComicFileType>()
            .map { format -> format.extension }.toSet()

        private fun isComic(document: DocumentFile): Boolean {
            return FileUtils.getExtension(document) in COMPRESSION_FORMATS &&
                    !FileUtils.naiveIsHidden(document.uri.toString())
        }
    }

    /**
     * Given a [DocumentFile] of a folder, scan for comics inside it.
     * The [coroutineScope] in which this function runs is called is also passed to check for activity.
     */
    fun scan(rootFolder: DocumentFile, coroutineScope: CoroutineScope): Flow<Array<DocumentFile?>> {
        return flow {
            // Emit in chunk of BUFFER_SIZE Comics
            val BUFFER_SIZE = 10

            // Emmit immediately the first IMMEDIATE_LIMIT Comics
            val IMMEDIATE_LIMIT = 10

            var buffer = arrayOfNulls<DocumentFile>(BUFFER_SIZE)
            var counter = 0
            var emitImmediate = true

            rootFolder.walkTopDown().forEach { document ->
                if (! coroutineScope.isActive) {
                    Log.w("yacvwtf", "Cancel scanning!")
                    return@flow
                }

                if (isComic(document)) {
                    Log.d("yacv", Uri.decode(document.uri.toString()))

                    if (emitImmediate) {
                        if (counter < IMMEDIATE_LIMIT) {
                            emit(arrayOf(document) as Array<DocumentFile?>)    // https://issuetracker.google.com/issues/175457638
                            counter++
                        } else {
                            emitImmediate = false
                            counter = 0
                        }
                    } else {
                        if (counter == BUFFER_SIZE) {
                            emit(buffer)
                            buffer = arrayOfNulls(BUFFER_SIZE)
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