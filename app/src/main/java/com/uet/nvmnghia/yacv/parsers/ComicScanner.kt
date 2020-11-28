package com.uet.nvmnghia.yacv.parsers

import android.os.Environment
import com.uet.nvmnghia.yacv.parsers.file.ComicParser
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class ComicScanner {
    companion object {
        private val COMPRESSION_FORMATS = enumValues<ComicParser.ComicFileType>()
                .map { type -> type.extension }.toSet()

        private fun isComic(file: File): Boolean {
            if (!file.isFile) return false
            return COMPRESSION_FORMATS.contains(file.extension.toLowerCase(Locale.ROOT))
        }

        /**
         * Scan the given folder.
         * If nothing is given, use the depreciated way :).
         *
         * @param folderPath Folder to scan for comic
         */
        fun scan(folderPath: String? = null): List<File> {
            val comics: MutableList<File> = ArrayList()

            // Param is val, i.e. no reassignment
            // https://stackoverflow.com/a/42540294/5959593
            val _folderPath = folderPath ?: Environment.getExternalStorageDirectory().canonicalPath.toString()

            File(_folderPath).walkTopDown()
                .forEach { file ->
                    if (isComic(file)) {
                        comics.add(file)
                    }
                }

            return comics
        }
    }
}