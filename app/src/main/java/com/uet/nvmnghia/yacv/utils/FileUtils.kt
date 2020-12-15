package com.uet.nvmnghia.yacv.utils

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
         * Get [DocumentFile] extension.
         */
        fun getExtension(document: DocumentFile): String? {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(document.type)
        }
    }
}
