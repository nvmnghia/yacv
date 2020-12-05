package com.uet.nvmnghia.yacv.parser.file.impl

import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.metadata.comicrack.ComicRackParser
import com.uet.nvmnghia.yacv.parser.metadata.generic.GenericMetadataParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import com.uet.nvmnghia.yacv.utils.NaturalOrderComparator
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class CBZParser(filePath: String) : ComicParser(filePath) {
    companion object {
        val COMPARATOR = object : NaturalOrderComparator<ZipEntry>() {
            override fun compare(o1: ZipEntry, o2: ZipEntry): Int {
                return STRING_COMPARATOR.compare(
                    // Special handling: consider 2 strings:
                    // A0001.jpg
                    // B0003-0004.jpg
                    // The first string should be before the second one.
                    // However, it is after because `.` is after `-`.
                    // Therefore extension must be trimmed.
                    o1.name.substringBeforeLast('.'),
                    o2.name.substringBeforeLast('.')
                )
            }
        }
    }

    private val zipFile: ZipFile = ZipFile(filePath)
    private val entries: List<ZipEntry> by lazy {
        zipFile.entries()
            .toList()
            .filter { entry -> !entry.isDirectory && FileUtils.isImage(entry.name) }
            .sortedWith(COMPARATOR)
    }

    override fun getPageInputStream(pageIdx: Int): InputStream {
        return zipFile.getInputStream(entries[pageIdx])
    }

    override fun _getNumOfPages(): Int {
        return entries.size
    }

    override fun getTypeEnum(): ComicFileType {
        return ComicFileType.CBZ
    }

    override fun parseInfo(): Comic {
        // First use a generic parser
        val comic = GenericMetadataParser.parse(filePath)

        // Then the more sophisticated one
        val comicInfoXml = zipFile.entries()
            .asSequence()
            .firstOrNull { entry -> entry.name.toLowerCase(Locale.ROOT) == "comicinfo.xml" }
        if (comicInfoXml != null) {
            ComicRackParser.parse(zipFile.getInputStream(comicInfoXml), comic)
        }

        return comic
    }

    override fun close() {
        zipFile.close()
    }

}