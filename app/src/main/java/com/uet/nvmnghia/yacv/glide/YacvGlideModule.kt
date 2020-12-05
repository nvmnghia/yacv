package com.uet.nvmnghia.yacv.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import java.io.InputStream


@GlideModule
class YacvGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(ComicParser.PageRequest::class.java, InputStream::class.java,
            CompressedImageModelLoaderFactory())
    }
}