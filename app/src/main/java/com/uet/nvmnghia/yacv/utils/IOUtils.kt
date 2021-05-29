package com.uet.nvmnghia.yacv.utils

import android.util.Log
import com.uet.nvmnghia.yacv.utils.IOUtils.Companion.DEFAULT_BUFFER_SIZE
import java.io.*
import java.util.*


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



        /**
         * Backport of readNBytes.
         *
         * Reads the requested number of bytes from the input stream into the given
         * byte array. This method blocks until {@code len} bytes of input data have
         * been read, end of stream is detected, or an exception is thrown. The
         * number of bytes actually read, possibly zero, is returned. This method
         * does not close the input stream.
         *
         * <p> In the case where end of stream is reached before {@code len} bytes
         * have been read, then the actual number of bytes read will be returned.
         * When this stream reaches end of stream, further invocations of this
         * method will return zero.
         *
         * <p> If {@code len} is zero, then no bytes are read and {@code 0} is
         * returned; otherwise, there is an attempt to read up to {@code len} bytes.
         *
         * <p> The first byte read is stored into element {@code b[off]}, the next
         * one in to {@code b[off+1]}, and so on. The number of bytes read is, at
         * most, equal to {@code len}. Let <i>k</i> be the number of bytes actually
         * read; these bytes will be stored in elements {@code b[off]} through
         * {@code b[off+}<i>k</i>{@code -1]}, leaving elements {@code b[off+}<i>k</i>
         * {@code ]} through {@code b[off+len-1]} unaffected.
         *
         * <p> The behavior for the case where the input stream is <i>asynchronously
         * closed</i>, or the thread interrupted during the read, is highly input
         * stream specific, and therefore not specified.
         *
         * <p> If an I/O error occurs reading from the input stream, then it may do
         * so after some, but not all, bytes of {@code b} have been updated with
         * data from the input stream. Consequently the input stream and {@code b}
         * may be in an inconsistent state. It is strongly recommended that the
         * stream be promptly closed if an I/O error occurs.
         *
         * @param  b the byte array into which the data is read
         * @param  off the start offset in {@code b} at which the data is written
         * @param  len the maximum number of bytes to read
         * @return the actual number of bytes read into the buffer
         * @throws IOException if an I/O error occurs
         * @throws NullPointerException if {@code b} is {@code null}
         * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len}
         *         is negative, or {@code len} is greater than {@code b.length - off}
         *
         * @since 9
         */
        fun readNBytes(input: InputStream, b: ByteArray, off: Int, len: Int): Int {
            if (b.size or off or len < 0 || len > b.size - off){
                throw IndexOutOfBoundsException("Range [$off, $off + $len) out of bounds for length ${b.size}")
            }

            var n = 0

            while (n < len) {
                val count: Int = input.read(b, off + n, len - n)
                if (count < 0) break
                n += count
            }

            return n
        }

        /**
         * Given an InputStream, read the last N byte into a buffer.
         * If the buffer doesn't have enough space for
         * The buffer is written over and over during the operation.
         * The InputStream won't be closed.
         *
         * @param input the InputStream to read from
         * @param buf the byte array into which the data is read
         * @param off the start offset in buf at which data is written
         * @param N number of last byte to read
         * @return actual number of last byte read
         */
        fun lastNBytes(input: InputStream, buf: ByteArray, off: Int, len: Int, N: Int): Int {
            if (buf.size or off or len < 0 || len > buf.size - off){
                throw IndexOutOfBoundsException("Range [$off, $off + $len) out of bounds for length ${buf.size}")
            }

            if (N > len) {
                throw IllegalArgumentException("The writable length len = $len " +
                        "is less than the desired number of last bytes N = $N")
            }

            if (N == 0) {
                // Without this early returns, the read while() runs forever with empty stream.
                return 0
            }

            val bis: InputStream = if (input !is BufferedInputStream) {
                val bufferSize = 256 * 1024
                BufferedInputStream(input, bufferSize)
            } else {
                input
            }

            val actualNumLastBytes: Int
            var readFully = false
            var count: Int

            while (bis.read(buf, off, N).also { count = it } == N) {
                readFully = true
            }

            if (readFully) {
                // The whole buffer has been filled at least once.
                actualNumLastBytes = N
                if (count == -1) {
                    // Current buffer is already filled with the last N bytes
                    // so do nothing more
                } else {
                    if (count < N / 2) {
                        // The last count bytes of the stream are the first count bytes in the buffer
                        // The N - count bytes before the last count bytes are the last N - count bytes in the buffer
                        val tmp = ByteArray(count)

                        // Copy first count bytes to tmp
                        System.arraycopy(buf, off, tmp, 0, count)

                        // Copy last N - count bytes to the front, overwrite what's already there
                        System.arraycopy(buf, off + count, buf, off, N - count)

                        // Copy tmp to the back
                        System.arraycopy(tmp, 0, buf, off + N - count, count)
                    } else {
                        // The reverse of the above case
                        val tmp = ByteArray(N - count)

                        // Copy last N - count bytes to tmp
                        System.arraycopy(buf, off + count, tmp, 0, N - count)

                        // Copy first count bytes to the back, overwrite what's already there
                        System.arraycopy(buf, off, buf, off + N - count, count)

                        // Copy tmp to the front
                        System.arraycopy(tmp, 0, buf, off, N - count)
                    }
                }
            } else {
                // buf isn't fully filled, but EOF has already been reached
                actualNumLastBytes = if (count == -1) {
                    // The input stream is empty, thus count = -1 in the first loop.
                    0
                } else {
                    count
                }
            }

            return actualNumLastBytes
        }

    }

}