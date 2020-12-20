package com.uet.nvmnghia.yacv.utils

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class IOUtils {

    companion object {
        /**
         * Read all [input] and write to [output].
         * Default [bufferSize] is 100KB, as the app mostly deals with images.
         */
        fun copy(input: InputStream, output: OutputStream, bufferSize: Int = 100 * (1 shl 20)): Boolean {
            val _bufferSize = if (bufferSize <= 0) {
                100 * (1 shl 20)
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