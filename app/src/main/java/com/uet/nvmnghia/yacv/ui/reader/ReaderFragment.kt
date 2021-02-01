package com.uet.nvmnghia.yacv.ui.reader

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.parser.file.ComicParserFactory
import dagger.hilt.android.AndroidEntryPoint
import java.net.URI


@AndroidEntryPoint
class ReaderFragment : Fragment() {

    private lateinit var fileUri: Uri
    private lateinit var parser: ComicParser

    val viewModel: ReaderViewModel by viewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var comicPageAdapter: ComicPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileUri = Uri.parse(requireArguments().getString("fileUri"))
        parser = ComicParserFactory.create(requireContext(), fileUri)!!

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

        viewModel.fileName.observe(viewLifecycleOwner,
            { (requireActivity() as AppCompatActivity).supportActionBar?.title = it })

        return view
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


class ReaderViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val fileUri: String = savedStateHandle.get<String>("fileUri")
        ?: throw IllegalArgumentException("Missing file URI when reading comic")

    private val _fileName = MutableLiveData(URI(fileUri).path.substringAfterLast('/'))
    val fileName: LiveData<String>
        get() = _fileName

}