package com.uet.nvmnghia.yacv.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates


@AndroidEntryPoint
class ComicPageViewerFragment : Fragment() {

    val viewModel: ComicPageViewerViewModel by viewModels()

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
        glide.load(viewModel.parser.requestPage(viewModel.pageNum))
            .into(comicImageView)

        return view
    }
    
    companion object {
        internal const val COMIC_URI = "fileUri"
        internal const val PAGE_NUM = "pageNum"

        fun newInstance(fileUri: String, pageNum: Int): ComicPageViewerFragment {
            val args = Bundle()
            args.putString(COMIC_URI, fileUri)
            args.putInt(PAGE_NUM, pageNum)

            val fragment = ComicPageViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}