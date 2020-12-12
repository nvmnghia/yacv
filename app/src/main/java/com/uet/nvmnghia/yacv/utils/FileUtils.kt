package com.uet.nvmnghia.yacv.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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



    }
}
