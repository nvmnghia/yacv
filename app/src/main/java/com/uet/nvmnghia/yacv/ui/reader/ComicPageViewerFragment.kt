package com.uet.nvmnghia.yacv.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.parser.file.ComicParser


class ComicPageViewerFragment(
    private val parser: ComicParser,
    private val pageNum: Int
) : Fragment() {

    private lateinit var glide: RequestManager
    private lateinit var comicImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glide = Glide.with(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reader_item_comic_page, container, false)

        comicImageView = view.findViewById(R.id.comic_imageview)
        glide.load(parser.requestPage(pageNum))
            .into(comicImageView)

        return view
    }
}