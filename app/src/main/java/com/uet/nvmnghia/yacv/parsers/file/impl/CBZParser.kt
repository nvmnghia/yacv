package com.uet.nvmnghia.yacv.parsers.file.impl

import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parsers.file.ComicParser
import com.uet.nvmnghia.yacv.parsers.metadata.ComicRackParser
import com.uet.nvmnghia.yacv.utils.FileUtils
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

    override fun getPageInputStream(pageIdx: Int): InputStream {
        TODO()
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
            .toList()
            .firstOrNull {entry ->
                entry.name.toLowerCase(Locale.ROOT) == "comicinfo.xml"}
        if (comicInfoXml != null) {
            ComicRackParser.parse(zipFile.getInputStream(comicInfoXml), comic)
        }

        return comic
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}