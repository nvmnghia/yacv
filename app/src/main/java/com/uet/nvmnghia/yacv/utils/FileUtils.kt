package com.uet.nvmnghia.yacv.utils

import android.content.Context
import android.net.Uri
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
         * Get [DocumentFile] extension in lowercase.
         * TODO: rewrite with magic byte check, or anything more robust
         */
        fun getExtension(document: DocumentFile): String? {
            return document.name
                ?.substringAfterLast('.')
                ?.toLowerCase(Locale.ROOT)
        }

        /**
         * Check if the given Uri exist.
         */
        fun isTreeExist(context: Context, uri: Uri): Boolean {
            return DocumentFile.fromTreeUri(context, uri)?.exists() == true
        }

        /**
         * Check if the given Uri can be read.
         */
        fun canReadTree(context: Context, uri: Uri): Boolean {
            return DocumentFile.fromTreeUri(context, uri)?.canRead() == true
        }

        /**
         * Given a file path, check if it is hidden naively using the leading dot rule.
         */
        fun naiveIsHidden(filePath: String): Boolean {
            return filePath
                .split('/')
                .firstOrNull { segment ->
                    segment.startsWith('.') &&
                            !(segment == "." || segment == "..")
                } != null
        }

        /**
         * Given a URI denoting a path, return the name of the resource.
         */
        inline fun folderNameFromPathUri(pathUri: String): String {
            val uri = if (pathUri.endsWith('/')) {
                Uri.parse(pathUri.substringBeforeLast('/'))
            } else {
                Uri.parse(pathUri)
            }

            return uri.schemeSpecificPart.substringAfterLast('/')
        }
    }
}
