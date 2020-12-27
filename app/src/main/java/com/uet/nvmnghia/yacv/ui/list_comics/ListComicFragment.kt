package com.uet.nvmnghia.yacv.ui.list_comics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.ui.helper.RecyclerItemClickListener
import com.uet.nvmnghia.yacv.utils.DeviceUtil
import com.uet.nvmnghia.yacv.utils.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates


@AndroidEntryPoint
class ListComicFragment: Fragment() {

    val viewModel: ListComicViewModel by viewModels()

    private lateinit var glide: RequestManager

    /**
     * [RecyclerView] of list comics & its [RecyclerView.Adapter].
     */
    private lateinit var listComicsInFolder: RecyclerView
    private lateinit var comicAdapter: ComicAdapter
    private lateinit var listComicsInFolderObserver: Observer<List<Comic>>

    /**
     * Number of columns for [listComicsInFolder],
     * set to [calculateNumberOfColumns]'s returned value.
     */
    private var NUM_COL by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glide = Glide.with(this)

        comicAdapter = ComicAdapter(glide)
        NUM_COL = calculateNumberOfColumns()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browse_folder, container, false)

        setupListComicFolders(view)

        listComicsInFolderObserver = Observer { comicAdapter.submitList(it) }
        viewModel.comics.observe(viewLifecycleOwner, listComicsInFolderObserver)

        return view
    }

    /**
     * Setup [listComicsInFolder].
     */
    private fun setupListComicFolders(view: View) {
        // General setup
        listComicsInFolder = view.findViewById(R.id.browse_folder_list_comics)

        listComicsInFolder.adapter = comicAdapter
        listComicsInFolder.layoutManager = GridLayoutManager(activity, NUM_COL)
        listComicsInFolder.setHasFixedSize(true)

        // Spacing
        val spacing = resources.getDimension(R.dimen.library_item_folder_spacing).toInt()
        listComicsInFolder.addItemDecoration(ThemeUtils.getRightBottomSpacer(spacing))

        // Click listener
        val clickListener = object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                onItemClick(position)
            }

            override fun onLongItemClick(view: View?, position: Int) {}
        }
        listComicsInFolder.addOnItemTouchListener(
            RecyclerItemClickListener(requireContext(), listComicsInFolder, clickListener))
    }

    /**
     * Callback when a comic is clicked.
     */
    private fun onItemClick(position: Int) {
        Toast.makeText(requireContext(),
            "Clicked at $comicAdapter.currentList[position].fileUri",
            Toast.LENGTH_SHORT).show()
    }

    /**
     * Calculate number of columns of [listComicsInFolder].
     */
    fun calculateNumberOfColumns(): Int {
        val screenWidth: Int = DeviceUtil.getScreenWidthInPx(requireContext())
        val columnWidth = resources.getDimension(R.dimen.browse_folder_item_comic_column_width).toInt()    // In pixel, already scaled.
        val numColumn = screenWidth / columnWidth
        return if (numColumn != 0) numColumn else 1
    }

}