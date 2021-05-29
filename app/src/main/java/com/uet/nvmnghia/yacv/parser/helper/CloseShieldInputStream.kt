package com.uet.nvmnghia.yacv.parser.helper

import java.io.FilterInputStream
import java.io.InputStream


/**
 * A wrapper that makes an [InputStream] non-closeable by [InputStream.close].
 * If the [InputStream] has to be closed, use [actuallyClose].
 */
class CloseShieldInputStream(input: InputStream) : FilterInputStream(input) {

    override fun close() {
        // Just ignore it
    }

    fun actuallyClose() = super.close()

}