package com.uet.nvmnghia.yacv.glide

import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import java.io.File
import java.io.IOException
import java.io.InputStream


class CompressedImageModelLoader : ModelLoader<ComicParser.PageRequest, InputStream> {
    /**
     * Check whether this one can be loaded.
     */
    override fun handles(model: ComicParser.PageRequest): Boolean {
        return try {
            File(model.uri)
            true
        } catch (ioe: IOException) {
            Log.w("Comic", ioe)
            false
        }
    }

    /**
     * Actually load the page.
     */
    override fun buildLoadData(
        model: ComicParser.PageRequest,
        width: Int,
        height: Int,
        options: Options,
    ): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(model), CompressedImageDataFetcher(model))
    }
}


class CompressedImageModelLoaderFactory : ModelLoaderFactory<ComicParser.PageRequest, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ComicParser.PageRequest, InputStream> {
        return CompressedImageModelLoader()
    }

    override fun teardown() {
        // No idea
    }
}