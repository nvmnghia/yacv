package com.uet.nvmnghia.yacv.parsers.file.impl

import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parsers.file.ComicParser
import com.uet.nvmnghia.yacv.parsers.metadata.ComicRackParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import com.uet.nvmnghia.yacv.utils.NaturalOrderComparator
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class CBZParser(filePath: String) : ComicParser(filePath) {

    private val zipFile: ZipFile = ZipFile(filePath)
    private val entries: List<ZipEntry> = zipFile.entries()
        .toList()
        .filter {entry ->
            !entry.isDirectory && FileUtils.isImage(entry.name)}
        .sortedWith(object : NaturalOrderComparator() {
            fun compare(z1: ZipEntry, z2: ZipEntry): Int {
                return compare(z1.name, z2.name)
            }
        })

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
        val comic = Comic(filePath)

        val comicInfoXml = zipFile.entries()
            .asSequence()
            .firstOrNull {entry ->
                entry.name.toLowerCase(Locale.ROOT) == "comicinfo.xml"}
        if (comicInfoXml != null) {
            ComicRackParser.parse(zipFile.getInputStream(comicInfoXml), comic)
        }

        return comic
    }

    override fun close() {
        zipFile.close()
    }
}