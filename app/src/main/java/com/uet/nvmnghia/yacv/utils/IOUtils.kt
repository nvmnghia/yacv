package com.uet.nvmnghia.yacv.utils

import android.util.Log
import com.uet.nvmnghia.yacv.utils.IOUtils.Companion.DEFAULT_BUFFER_SIZE
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class IOUtils {

    companion object {
        /**
         * Default buffer size for copying images: 100KB.
         */
        const val DEFAULT_BUFFER_SIZE = 200 * (1 shl 10)

        /**
         * Read all [input] and write a [ByteArrayOutputStream] and return it.
         * Default [bufferSize] is [DEFAULT_BUFFER_SIZE], as the app mostly deals with images.
         */
        fun copyToMemory(input: InputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): ByteArrayOutputStream {
            val baos = ByteArrayOutputStream()
            copy(input, baos)
            return baos
        }

        /**
         * Read all [input] and write to [output].
         * Default [bufferSize] is [DEFAULT_BUFFER_SIZE], as the app mostly deals with images.
         */
        fun copy(input: InputStream, output: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Boolean {
            val _bufferSize = if (bufferSize <= 0) {
                DEFAULT_BUFFER_SIZE
            } else {
                bufferSize
            }

            val buffer = ByteArray(_bufferSize)
            var len: Int

            try {
                while (true) {
                    len = input.read(buffer)
                    if (len <= -1) {
                        break
                    }
                    output.write(buffer, 0, len)
                }
            } catch (ioe: IOException) {
                Log.e("yacv", "Cannot transfer data from InputStream to OutputStream when reading comic")
                return false
            }

            return true
        }
    }

}