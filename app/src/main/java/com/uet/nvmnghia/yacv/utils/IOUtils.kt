package com.uet.nvmnghia.yacv.utils

import android.util.Log
import com.uet.nvmnghia.yacv.utils.IOUtils.Companion.DEFAULT_BUFFER_SIZE
import java.io.*


class IOUtils {

    companion object {
        /**
         * Default buffer size for copying images: 200KB.
         */
        const val DEFAULT_BUFFER_SIZE = 200 * (1 shl 10)

        /**
         * Read all [input] and write to a [ByteArrayOutputStream] then return it.
         * Default [bufferSize] is [DEFAULT_BUFFER_SIZE], as the app mostly deals with images.
         */
        fun toOutputStream(input: InputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): ByteArrayOutputStream {
            val baos = ByteArrayOutputStream()
            copy(input, baos)
            return baos
        }

        /**
         * Read all [input] and write to a [ByteArrayInputStream] then return it.
         * TODO: At one point this doubles the memory consumption of baos,
         *  due to Array:copyOf making a new ByteArray copy.
         */
        fun toInputStream(input: InputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): ByteArrayInputStream {
            val baos = toOutputStream(input, bufferSize)
            return ByteArrayInputStream(baos.toByteArray())
        }

        /**
         * Convenient wrapper for [InputStream.copyTo], with [DEFAULT_BUFFER_SIZE].
         */
        fun copy(input: InputStream, output: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Boolean {
            try {
                input.copyTo(output, bufferSize)
            } catch (ioe: IOException) {
                Log.e("yacv", "Cannot transfer data from InputStream to OutputStream")
                return false
            }

            return true
        }
    }

}