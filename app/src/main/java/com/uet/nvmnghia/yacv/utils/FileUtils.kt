package com.uet.nvmnghia.yacv.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.util.*


class FileUtils {
    companion object {

        private val IMAGE_FORMATS = setOf("jpg", "jpeg", "bmp", "gif", "png", "webp")

        /**
         * Given a file name, check if it is an image, simply by extension.
         * This method does not validate whatsoever the file integrity.
         *
         * @param fileName File name of the image
         * @return Whether the file is an image or not
         */
        fun isImage(fileName: String): Boolean {
            return IMAGE_FORMATS.contains(
                fileName.substringAfterLast('.').toLowerCase(Locale.ROOT))
        }

        /**
         * Check if the given URI can be read or write.
         */
        fun canRead(context: Context, uri: Uri): Boolean {
            if (! DocumentFile.isDocumentUri(context, uri)) {
                return false
            }

            try {
                return DocumentFile.fromTreeUri(context, uri)?.canRead() == true
            } catch (e: Exception) {}

            try {
                return DocumentFile.fromSingleUri(context, uri)?.canRead() == true
            } catch (e: Exception) {}

            return false
        }

        /**
         * Given a [DocumentFile] [documentFile], get its extension in lowercase.
         */
        fun getExtension(documentFile: DocumentFile): String? {
            return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(documentFile.type)
                ?.toLowerCase(Locale.ROOT)
        }
    }
}
