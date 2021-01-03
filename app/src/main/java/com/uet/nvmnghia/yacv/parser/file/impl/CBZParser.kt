package com.uet.nvmnghia.yacv.parser.file.impl

import android.content.Context
import android.net.Uri
import com.uet.nvmnghia.yacv.parser.file.ArchiveParser
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.helper.CloseableIterator
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream


/**
 * Parser for CBZ.
 */
class CBZParser(
    private val context: Context,
    private val uri: Uri
) : ArchiveParser {

    override fun extractTo(folder: File) {
        TODO("Not yet implemented")
    }

    override fun getType(): ComicParser.ComicFileType {
        return ComicParser.ComicFileType.CBZ
    }

    override val entries: CloseableIterator<ArchiveParser.ArchiveEntry>
        get() {
            return object : CloseableIterator<ArchiveParser.ArchiveEntry> {
                private val zipIS = ZipInputStream(context.contentResolver.openInputStream(uri))

                private var start = true

                private var currentEntry: java.util.zip.ZipEntry? = null

                private var nexted = false

                override fun hasNext(): Boolean {
                    if (start || nexted) {
                        currentEntry = zipIS.nextEntry
                        nexted = false
                        start = false
                    }

                    return currentEntry != null
                }

                override fun next(): ZipEntry {
                    if (!nexted) {
                        nexted = true
                    } else {
                        currentEntry = zipIS.nextEntry
                    }

                    if (currentEntry == null) {
                        throw NoSuchElementException("CBZParser iterator already reached its end")
                    }

                    return ZipEntry(currentEntry!!.name, zipIS)
                }

                override fun close() {
                    zipIS.close()
                }
            }
        }

    class ZipEntry(
        override val path: String,
        override val inputStream: InputStream,
    ) : ArchiveParser.ArchiveEntry

}