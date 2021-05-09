package com.uet.nvmnghia.yacv.parser.file.impl.cbz

import android.content.Context
import android.net.Uri
import com.uet.nvmnghia.yacv.parser.file.ArchiveParser
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.helper.InputStreamGenerator
import com.uet.nvmnghia.yacv.utils.FileUtils
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream


/**
 * Parser for CBZ.
 */
class CBZParser(
    private val context: Context,
    private val uri: Uri,
    private val layout: Map<String, Int>    // Map entry name/internal path to offset
) : ArchiveParser {

    constructor(context: Context, uri: Uri) :
            this(context, uri, createMapEntryToOffset(context, uri))

    override fun extractTo(folder: File) {
        TODO("Not yet implemented")
    }

    override fun getType(): ComicParser.ComicFileType =
        ComicParser.ComicFileType.CBZ

    override fun getEntryNames(): List<String> = layout.keys.toList()

    override fun getInputStream(entryName: String): InputStream {
        val offset = layout[entryName]!!
        val input = ComicParser.getFileInputStream(context, uri)
        input.skip(offset.toLong())
        return ZipArchiveInputStream(input)
    }

    override fun getLayout(): Map<String, Int> = layout

    companion object {
        fun createMapEntryToOffset(context: Context, uri: Uri) : Map<String, Int> {
            val genIS = InputStreamGenerator {
                BufferedInputStream(context.contentResolver.openInputStream(uri), 8192)
            }
            val zb = ZipBuffer(genIS)
            val zf = ZipFile(zb)
            val entryToOffset = mutableMapOf<String, Int>()

            for (entry: ZipArchiveEntry in zf.entries) {
                if (entry.isDirectory || FileUtils.naiveIsHidden(entry.name)) {
                    continue
                }

                entryToOffset[entry.name] = entry.localHeaderOffset.toInt()
            }

            return entryToOffset
        }
    }

}