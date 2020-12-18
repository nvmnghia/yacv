package com.uet.nvmnghia.yacv.parser

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.helper.walkTopDown
import com.uet.nvmnghia.yacv.utils.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class ComicScanner @Inject constructor(val context: Context) {

    companion object {
        private val COMPRESSION_FORMATS = enumValues<ComicParser.ComicFileType>()
            .map { format -> format.extension }.toSet()

        private fun isComic(document: DocumentFile): Boolean {
            return FileUtils.getExtension(document) in COMPRESSION_FORMATS
        }

        private val TEST_ROOT_FOLDER_URI = Uri.parse(
            "content://com.android.externalstorage.documents/tree/home%3A")
    }

    /**
     * Given a [Uri] pointing to a folder, scan for comics inside it.
     * If nothing is given, use TEST_COMIC_URI.
     *
     * @param uri URI to scan for comic
     */
    fun scan(uri: Uri = TEST_ROOT_FOLDER_URI): Flow<Array<DocumentFile?>> {
        return flow {
            // Emit in chunk
            val BUFFER_SIZE = 10
            var buffer = arrayOfNulls<DocumentFile>(BUFFER_SIZE)
            var counter = 0

            DocumentFile.fromTreeUri(context, uri)?.walkTopDown()?.forEach { document ->
                if (isComic(document)) {
                    if (counter == BUFFER_SIZE) {
                        emit(buffer)
                        buffer = arrayOfNulls(BUFFER_SIZE)
                        counter = 0
                    }

                    buffer[counter] = document
                    counter++
                }
            }

            // Emit the rest
            emit(buffer)
        }
    }

}