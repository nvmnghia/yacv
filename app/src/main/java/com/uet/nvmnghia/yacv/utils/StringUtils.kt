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
         */
        fun nameFromPath(path: String): String {
            return if (path.endsWith('/')) {
                path.substringBeforeLast('/')
                    .substringAfterLast('/')
            } else {
                path.substringAfterLast('/')
            }
        }

        /**
         * Check string equality, but work backward.
         */
        fun equalBackward(s1: String, s2: String): Boolean {
            if (s1.length != s2.length) {
                return false
            }

            for (i in s1.indices.reversed()) {
                if (s1[i] != s2[i]) {
                    return false
                }
            }

            return true
        }
    }
}