package com.uet.nvmnghia.yacv.parser.file.impl.cbz

import android.os.Build
import androidx.annotation.RequiresApi
import com.uet.nvmnghia.yacv.parser.helper.InputStreamGenerator
import com.uet.nvmnghia.yacv.utils.IOUtils
import org.apache.commons.compress.utils.CountingInputStream
import java.io.InputStream
import java.io.PushbackInputStream
import java.nio.ByteBuffer
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel


/**
 * The buffer emulating a file for Apache Commons Compress ZipFile.
 * The buffer tries to read some part at the start and end so that when
 * ZipFile constructor is invoked, nothing fails.
 * If fails, increase (say double) endBufferSize and do it again (and again...).
 */
@RequiresApi(Build.VERSION_CODES.N)
class ZipBuffer(
    private val generator: InputStreamGenerator,
    endBufferSize: Int = DEFAULT_END_BUFFER_SIZE
) : SeekableByteChannel {

    private var secondChance: InputStream
    private var secondChancePosition = 0

    /**
     * Buffer for the whole file if possible.
     * If wholeFile is not null, the whole file is stored in buffer,
     * so there's no need for both startBuf and endBuf.
     */
    private var wholeFile: ByteArray? = null

    /**
     * Buffer for the start part, size is fixed at START_BUFFER_SIZE.
     * Exist only if wholeFile is null.
     */
    private var startBuf: ByteArray? = null

    /**
     * Buffer for the end part, size will be configured in constructor.
     * Exist only if wholeFile is null;
     */
    private var endBuf: ByteArray? = null
    private var minPositionBufferedInEndBuf = 0

    private var size = 0
    private var position = 0

    private var isOpen = true

    init {
        require(endBufferSize >= MIN_END_BUFFER_SIZE) {
            "endBufferSize = $endBufferSize smaller than MIN_END_BUFFER_SIZE = $MIN_END_BUFFER_SIZE"
        }

        secondChance = generator.generate()

        val cis = CountingInputStream(this.generator.generate())
        PushbackInputStream(cis, START_BUFFER_SIZE).use { pis ->
            startBuf = ByteArray(START_BUFFER_SIZE)
            val actualRead: Int = IOUtils.readNBytes(pis, startBuf!!, 0, START_BUFFER_SIZE)
            pis.unread(startBuf)

            if (actualRead < START_BUFFER_SIZE) {
                // The whole file is inside startBuf
                wholeFile = ByteArray(cis.bytesRead.toInt())
                System.arraycopy(
                    startBuf!!, 0,
                    wholeFile!!, 0,
                    wholeFile!!.size)

                startBuf = null
                return@use
            }

            endBuf = ByteArray(endBufferSize)
            IOUtils.lastNBytes(pis, endBuf!!, 0, endBufferSize, endBufferSize)

            if (cis.bytesRead <= endBufferSize) {
                // The whole file is inside endBuf
                wholeFile = ByteArray(cis.bytesRead.toInt())
                System.arraycopy(
                    endBuf!!, 0,
                    wholeFile!!, 0,
                    wholeFile!!.size)

                startBuf = null
                endBuf = null
                return@use
            }

            if (cis.bytesRead <= endBufferSize + START_BUFFER_SIZE) {
                wholeFile = ByteArray(cis.bytesRead.toInt())
                System.arraycopy(
                    startBuf!!, 0,
                    wholeFile!!, 0,
                    START_BUFFER_SIZE)
                System.arraycopy(
                    endBuf!!, 0,
                    wholeFile!!, START_BUFFER_SIZE,
                    cis.bytesRead.toInt() - START_BUFFER_SIZE)

                startBuf = null
                endBuf = null
                return@use
            }

            minPositionBufferedInEndBuf = (cis.bytesRead - endBufferSize).toInt()
        }

        size = cis.bytesRead.toInt()
    }


    override fun read(dst: ByteBuffer): Int {
        checkPositionAndThrow()

        if (position >= size) {
            return -1 // EOF
        }

        val bufferToRead: ByteArray?
        var positionToRead = position

        when {
            wholeFile != null -> {
                bufferToRead = wholeFile
            }
            position + dst.remaining() < startBuf!!.size -> {
                bufferToRead = startBuf
            }
            position >= minPositionBufferedInEndBuf -> {
                bufferToRead = endBuf
                positionToRead = position - minPositionBufferedInEndBuf
            }
            else -> {
                bufferToRead = null
            }
        }

        if (bufferToRead != null) {
            val length = bufferToRead.size.coerceAtMost(dst.remaining())
            dst.put(bufferToRead, positionToRead, length)
            position += length
            return length
        }

        askForSecondChance(position)

        val actuallyRead: Int = secondChance.read(dst.array(), dst.position(), dst.remaining())
        if (actuallyRead == -1) {
            return actuallyRead
        }

        dst.position(dst.limit())
        position += actuallyRead
        secondChancePosition += actuallyRead
        return actuallyRead
    }

    override fun write(src: ByteBuffer?): Int = throw NonWritableChannelException()

    override fun position(): Long = position.toLong()

    override fun position(newPosition: Long): SeekableByteChannel {
        checkPositionAndThrow(newPosition)
        position = newPosition.toInt()
        return this
    }

    override fun size(): Long = size.toLong()

    override fun truncate(size: Long): SeekableByteChannel = throw NonWritableChannelException()

    override fun close() {
        isOpen = false
    }

    override fun isOpen(): Boolean = isOpen

    /**
     * Generate another [secondChance] [InputStream].
     */
    private fun askForSecondChance(position: Int) {
        var toSkip: Int
        if (secondChancePosition < position) {
            toSkip = position - secondChancePosition
        } else {
            secondChance.close()
            secondChance = generator.generate()
            toSkip = position
        }

        var actuallySkipped: Int
        do {
            actuallySkipped = secondChance.skip(toSkip.toLong()).toInt()
            toSkip -= actuallySkipped
        } while (actuallySkipped != 0)

        secondChancePosition = position
    }

    /**
     * Check the position.
     * Position can be >= size as per the contract, but in those cases all reads return EOF.
     * Position CAN be in unavailable area, as we have secondChance now.
     *
     * @param position position before an operation.
     */
    private fun checkPositionAndThrow(position: Long = this.position.toLong()) {
        require(position >= 0) { "Negative position" }
    }

    companion object {
        private const val DEFAULT_END_BUFFER_SIZE = 1 * 1024 * 1024
        private const val MIN_END_BUFFER_SIZE = 65557    // See ANALYSIS.md
        private const val START_BUFFER_SIZE = 30 +       // Local file header fixed fields length
                (1 shl 16 - 1) +    // Max file name length
                (1 shl 16 - 1)      // Max extra fields length
    }

}