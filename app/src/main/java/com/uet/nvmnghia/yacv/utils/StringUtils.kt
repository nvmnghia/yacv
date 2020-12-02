package com.uet.nvmnghia.yacv.utils

import java.util.regex.Pattern


class StringUtils {
    companion object {
        val CONSECUTIVE_SPACES = Pattern.compile("\\s{2,}")

        /**
         * Trim & replace consecutive spaces.
         */
        fun normalizeSpaces(str: String): String {
            return CONSECUTIVE_SPACES.matcher(str)
                .replaceAll(" ")
                .replace('\t', ' ')
                .trim()
        }
    }
}