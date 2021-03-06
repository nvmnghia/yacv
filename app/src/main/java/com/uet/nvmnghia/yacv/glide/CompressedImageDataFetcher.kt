package com.uet.nvmnghia.yacv.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import java.io.InputStream
import java.util.zip.ZipInputStream


/**
 * Each model will have its own DataFetcher, instead of a generic one.
 */
class CompressedImageDataFetcher(
    private val pageRequest: ComicParser.PageRequest,
) : DataFetcher<InputStream> {

    private lateinit var zis: ZipInputStream

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        val input = ComicParser.getFileInputStream(pageRequest.context, pageRequest.uri)
        input.skip(pageRequest.offset.toLong())

        zis = ZipInputStream(input)
        zis.nextEntry

        callback.onDataReady(zis)
    }

    override fun cleanup() {
        zis.close()
    }

    override fun cancel() {
        // Load to death!
    }

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource(): DataSource = DataSource.REMOTE

}