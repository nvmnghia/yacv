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
    }
}