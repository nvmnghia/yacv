package com.uet.nvmnghia.yacv.utils

import java.util.regex.Pattern


class StringUtils {
    companion object {
        val CONSECUTIVE_SPACES = Pattern.compile("\\s{2,}")

        /**
         * Trim & replace consecutive spaces.
         * Mainly used to normalize strings before inserting into DB,
         * as the default tokenizer can't handle Unicode whitespaces.
         * TODO: Check if the unicode61 tokenizer can do this all in DB.
         */
        fun normalizeSpaces(str: String): String {
            return CONSECUTIVE_SPACES.matcher(str)
                .replaceAll(" ")
                .replace('\t', ' ')
                .trim()
        }

        /**
         * Given a path-like string (containing '/'), return the name of the file.
         * Note that it doesn't work with folder name, which may end with '/'.
         */
        inline fun fileNameFromPath(path: String?): String? {
            return path?.substringAfterLast('/')
        }
    }
}