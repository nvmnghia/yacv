package com.uet.nvmnghia.yacv.ui.reader

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uet.nvmnghia.yacv.parser.file.ComicParser


class ComicPageAdapter(fragment: Fragment,
                       val parser: ComicParser) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return parser.pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return ComicPageViewerFragment.newInstance(parser.uri.toString(), position)
    }

}