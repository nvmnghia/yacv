package com.uet.nvmnghia.yacv.ui.reader

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.file.ComicParserFactory
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReaderFragment : Fragment() {

    private lateinit var fileUri: Uri
    private lateinit var parser: ComicParser

    private lateinit var viewPager: ViewPager2
    private lateinit var comicPageAdapter: ComicPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileUri = Uri.parse(requireArguments().getString("fileUri"))
        parser = ComicParserFactory.create(requireContext(),
            DocumentFile.fromSingleUri(requireContext(), fileUri))!!

        comicPageAdapter = ComicPageAdapter(this, parser)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reader, container, false)

        viewPager = view.findViewById(R.id.comic_viewpager)
        viewPager.adapter = comicPageAdapter

        return view
    }

    override fun onDestroy() {
        super.onDestroy()

        // TODO: Somehow tie parser.close() to onDestroy() instead of manual calling
        parser.close()
    }
}