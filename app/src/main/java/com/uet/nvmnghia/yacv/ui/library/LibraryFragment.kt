package com.uet.nvmnghia.yacv.ui.library

import android.app.SearchManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.covercache.CoverCache
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.ui.helper.RecyclerItemClickListener
import com.uet.nvmnghia.yacv.ui.library.LibraryViewModel.TextState.*
import com.uet.nvmnghia.yacv.utils.DeviceUtil
import com.uet.nvmnghia.yacv.utils.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class LibraryFragment : Fragment() {

    // TODO: Move it into viewModel?
    @Inject
    lateinit var comicDao: ComicDao

    @Inject
    lateinit var coverCache: CoverCache

    // Use ViewModelProvider to create ViewModel
    // https://developer.android.com/codelabs/kotlin-android-training-view-model#4
//        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)
    // ViewModel must be accessed in attached mode, therefore the above line
    // must be inside onViewCreated
    // However, we want to use Hilt for DI
    // Therefore the above method doesn't work anymore
    // The correct way is the below line
    val viewModel: LibraryViewModel by viewModels()

    private lateinit var glide: RequestManager

    /**
     * [RecyclerView] of list comic folders & its [RecyclerView.Adapter].
     */
    private lateinit var listComicFolders: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var listComicFoldersObserver: Observer<List<Folder>>

    /**
     * Number of column for [listComicFolders],
     * set to [calculateNumberOfColumns]'s returned value.
     */
    private var NUM_COL by Delegates.notNull<Int>()

    /**
     * [TextView] displayed when [listComicFolders] is empty,
     * showing the reason why there's no list.
     */
    private lateinit var noListTextView: TextView

    /**
     * Activity launchers
     */
    private lateinit var folderPickerLauncher: ActivityResultLauncher<Uri?>


    //================================================================================
    // Lifecycle hooks
    //================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Inject this shit
        glide = Glide.with(this)
            .setDefaultRequestOptions(
                RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565)    // In this fragment, Glide only loads cover
            )

        folderAdapter = FolderAdapter(glide, comicDao, coverCache)
        NUM_COL = calculateNumberOfColumns()

        HandleNoListTextView.SPAN_TEXT_COLOR = ThemeUtils.getPrimaryColor(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        setHasOptionsMenu(true)

        setupListComicFolders(view)

        registerActivityResultCallbacks()

        listComicFoldersObserver = Observer { folderAdapter.submitList(it) }
        viewModel.folders.observe(viewLifecycleOwner, listComicFoldersObserver)
        viewModel.textState.observe(viewLifecycleOwner, this::toggleEmptyListText)

        return view
    }

    private fun registerActivityResultCallbacks() {
        folderPickerLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree(),
            this::handleFolderPickerResult)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Remember to include in onCreateView
        // setHasOptionsMenu(true)
        inflater.inflate(R.menu.library_toolbar, menu)

        // Get the SearchView and set the searchable configuration
        requireActivity().let { activity ->
            val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            val searchableInfo = searchManager.getSearchableInfo(activity.componentName)
            (menu.findItem(R.id.library_toolbar_search).actionView as SearchView)
                .setSearchableInfo(searchableInfo)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.library_toolbar_select_root_folder -> changeRootScanFolder()
        }

        return true
    }


    //================================================================================
    // Handle TextView
    //================================================================================

    /**
     * Hide/show empty comic text.
     * When shown, there could be several values:
     * - No folder selected
     * - Cannot read folder (possibly deleted)
     * - No read permission
     * - No comic found
     */
    private fun toggleEmptyListText(state: LibraryViewModel.TextState) {
        noListTextView.text = when (state) {
            NO_ROOT -> {
                HandleNoListTextView.noRoot(
                    resources, this::changeRootScanFolder)
            }
            NO_COMIC -> {
                HandleNoListTextView.noComic(
                    resources, this::changeRootScanFolder)
            }
            else -> {
                // Do nothing, it is handled below.
                null
            }
        }

        when (state) {
            NO_TEXT -> {
                listComicFolders.visibility = View.VISIBLE
                noListTextView.visibility = View.GONE
                viewModel.folders.observe(viewLifecycleOwner, listComicFoldersObserver)
            }
            else -> {
                listComicFolders.visibility = View.GONE
                noListTextView.visibility = View.VISIBLE
                viewModel.folders.removeObserver(listComicFoldersObserver)
            }
        }
    }


    //================================================================================
    // Recycler view stuffs
    //================================================================================

    /**
     * Setup [listComicFolders].
     */
    private fun setupListComicFolders(view: View) {
        // General setup
        listComicFolders = view.findViewById(R.id.library_list_folders)
        listComicFolders.adapter = folderAdapter
        listComicFolders.layoutManager = GridLayoutManager(activity, NUM_COL)
        listComicFolders.setHasFixedSize(true)

        // Spacing
        val spacing = resources.getDimension(R.dimen.library_item_folder_spacing).toInt()
        listComicFolders.addItemDecoration(ThemeUtils.getRightBottomSpacer(spacing))

        // Click listener
        val clickListener = object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                onItemClick(position)
            }

            override fun onLongItemClick(view: View?, position: Int) {}
        }
        listComicFolders.addOnItemTouchListener(
            RecyclerItemClickListener(requireContext(), listComicFolders, clickListener))

        // Text displayed if empty list
        noListTextView = view.findViewById(R.id.library_no_list_info)
        noListTextView.movementMethod =
            LinkMovementMethod.getInstance()    // TODO: For clickable span?
    }

    /**
     * Callback when a folder is clicked.
     */
    private fun onItemClick(position: Int) {
        val comic = folderAdapter.currentList[position]
        val action = LibraryFragmentDirections
            .actionNavFragmentLibraryToListComicFragment(comic)
        findNavController().navigate(action)
    }

    /**
     * Calculate number of columns of [listComicFolders].
     */
    private fun calculateNumberOfColumns(): Int {
        val screenWidth: Int = DeviceUtil.getScreenWidthInPx(requireContext())
        val columnWidth = resources.getDimension(R.dimen.library_item_folder_column_width)
            .toInt()    // In pixel, already scaled.
        val numColumn = screenWidth / columnWidth
        return if (numColumn != 0) numColumn else 1
    }


    //================================================================================
    // Storage & scan
    //================================================================================

    /**
     * Select a root folder. If not granted, ask for READ_EXTERNAL_STORAGE.
     */
    private fun changeRootScanFolder() =
        launchFolderPicker()

    /**
     * Launch folder picker.
     * If a specific location needs to be opened first, pass its URI to the function.
     */
    private fun launchFolderPicker(suggestedUri: Uri? = null) =
        folderPickerLauncher.launch(suggestedUri)

    /**
     * Callback to handle folder picker result.
     * Given the [folderUri] from the picker, pass it to [viewModel].
     */
    private fun handleFolderPickerResult(folderUri: Uri?) =
        folderUri?.let { viewModel.rootFolderUri = it }

}
