package com.uet.nvmnghia.yacv.ui.reader

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.ui.reader.comicpage.ComicPageAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReaderFragment : Fragment() {

    private lateinit var fileUri: Uri
    private lateinit var parser: ComicParser

    val viewModel: ReaderViewModel by viewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var comicPageAdapter: ComicPageAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reader, container, false)

        setHasOptionsMenu(true)

        viewPager = view.findViewById(R.id.comic_viewpager)

        viewModel.comic.observe(viewLifecycleOwner) { comic ->
            fileUri = Uri.parse(comic.fileUri)
            parser = ComicParser(requireContext(), fileUri)

            comicPageAdapter = ComicPageAdapter(this, parser)

            viewPager.adapter = comicPageAdapter
        }

        viewModel.fileName.observe(viewLifecycleOwner)
            { fileName -> (requireActivity() as AppCompatActivity).supportActionBar?.title = fileName }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reader_toolbar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reader_toolbar_info -> viewModel.comic.value!!.let {
                val action = ReaderFragmentDirections.actionReaderFragmentToMetadataFragment(it.id)
                findNavController().navigate(action)
            }
        }

        return true
    }

    /**
     * Set action bar text to marquee.
     * Currently not used, added for future reference when rewriting reader UI.
     */
    private fun setActionBarTitleAsMarquee() {
        // Get Action Bar's title
        val decor: View = requireActivity().window.decorView
        val resId = resources.getIdentifier("action_bar_title", "id", "android")
        val title = decor.findViewById<TextView>(resId)    // redId is returned, but findViewById returns null.
                                                           // Is it because decor is not high enough in the hierarchy?

        // Set the ellipsize mode to MARQUEE and make it scroll only once
        title.ellipsize = TextUtils.TruncateAt.MARQUEE
        title.marqueeRepeatLimit = -1

        // In order to start scrolling, it has to be focused
        title.isFocusable = true
        title.isFocusableInTouchMode = true
        title.requestFocus()
    }

}
