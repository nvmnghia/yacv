package com.uet.nvmnghia.yacv.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.file.ComicParserFactory
import java.io.InputStream


/**
 * Each model will have its own DataFetcher, instead of a generic one.
 */
class CompressedImageDataFetcher(
    private val pageRequest: ComicParser.PageRequest
) : DataFetcher<InputStream> {

    private lateinit var parser: ComicParser

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        parser = ComicParserFactory.create(pageRequest.document)
        callback.onDataReady(parser.readPage(pageRequest.pageIdx))
    }

    override fun cleanup() {
        parser.close()
    }

    override fun cancel() {
        // Load to death!
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

}