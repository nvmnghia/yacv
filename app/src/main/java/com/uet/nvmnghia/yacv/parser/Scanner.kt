package com.uet.nvmnghia.yacv.parser

import android.os.Environment
import android.util.Log
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class Scanner {
    companion object {
        private val COMPRESSION_FORMATS = setOf("cbr", "cbz")

        private fun isComic(file: File): Boolean {
            if (!file.isFile) return false
            return COMPRESSION_FORMATS.contains(file.extension.toLowerCase(Locale.ROOT))
        }

        fun scan(): List<File> {
            val comics: MutableList<File> = ArrayList()

            val extPath = Environment.getExternalStorageDirectory().absolutePath.toString()
            File(extPath).walkTopDown()
                .forEach { file ->
                    if (isComic(file)) {
                        comics.add(file)
                    }
                }

            return comics
        }
    }
}