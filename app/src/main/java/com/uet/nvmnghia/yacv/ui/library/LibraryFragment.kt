package com.uet.nvmnghia.yacv.ui.library


import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.utils.DeviceUtil
import com.uet.nvmnghia.yacv.utils.RecyclerItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LibraryFragment : Fragment() {

    @Inject
    lateinit var comicDao: ComicDao

    // Use ViewModelProvider to create ViewModel
    // https://developer.android.com/codelabs/kotlin-android-training-view-model#4
//        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)
    // ViewModel must be accessed in attached mode, therefore the above line
    // must be inside onViewCreated
    // However, we want to use Hilt for DI
    // Therefore the above method doesn't work anymore
    // The correct way is the below line
    val viewModel: LibraryViewModel by viewModels()

    lateinit var glide: RequestManager

    lateinit var folderAdapter: FolderAdapter
    var NUM_COL: Int? = null

    lateinit var folderPickerLauncher: ActivityResultLauncher<Uri>
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    //================================================================================
    // Lifecycle hooks
    //================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Inject this shit
        glide = Glide.with(this)

        folderAdapter = FolderAdapter(glide, comicDao)
        NUM_COL = calculateNumberOfColumns()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        setHasOptionsMenu(true)

        setupListComicFolders(view)

        registerActivityResultCallbacks()

        viewModel.folders.observe(viewLifecycleOwner, folderAdapter::submitList)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Remember to include in onCreateView
        // setHasOptionsMenu(true)
        inflater.inflate(R.menu.library_toolbar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.library_toolbar_select_root_folder -> changeRootScanFolder()

        }

        return true
    }

    /**
     * Setup [RecyclerView] for list comic folders
     */
    private fun setupListComicFolders(view: View) {
        // General setup
        val listComicFolders: RecyclerView = view.findViewById(R.id.library_list_folders)
        listComicFolders.adapter = folderAdapter
        listComicFolders.layoutManager = GridLayoutManager(activity, NUM_COL!!)
        listComicFolders.setHasFixedSize(true)

        // Spacing
        val SPACING = resources.getDimension(R.dimen.library_item_folder_spacing).toInt()
        val spacer = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                // Trick:
                // - in library_list_folders, pad left and top only
                // - in this spacing code, add right and bottom spacing only
                outRect.right  = SPACING
                outRect.bottom = SPACING
            }
        }
        listComicFolders.addItemDecoration(spacer)

        // Click listener
        val clickListener = object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                Toast.makeText(requireContext(),
                    "Clicked at ${folderAdapter.currentList[position].path}",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onLongItemClick(view: View?, position: Int) {}
        }
        listComicFolders.addOnItemTouchListener(
            RecyclerItemClickListener(requireContext(), listComicFolders, clickListener))
    }

    /**
     * Calculate number of column in the [RecyclerView].
     */
    private fun calculateNumberOfColumns(): Int {
        val screenWidth: Int = DeviceUtil.getScreenWidthInPx(requireContext())
        val columnWidth = resources.getDimension(R.dimen.library_item_folder_column_width).toInt()    // In pixel, already scaled.
        val numColumn = screenWidth / columnWidth
        return if (numColumn != 0) numColumn  else 1
    }


    //================================================================================
    // Activity result callbacks
    //================================================================================

    private fun registerActivityResultCallbacks() {
        setupFolderPickerLauncher()
        setupRequestPermissionLauncher()
    }

    private fun setupFolderPickerLauncher() {
        folderPickerLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { folderUri ->
            folderUri?.path?.let { viewModel.rescanComics(it) }
        }
    }

    private fun setupRequestPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                folderPickerLauncher.launch(null)
            }
        }
    }

    /**
     * Select a root folder. If not granted, ask for READ_EXTERNAL_STORAGE.
     */
    private fun changeRootScanFolder() {
        when {
            ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.rescanComics()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                explainStoragePermission()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Show a dialog explaining why yacv needs storage permission.
     */
    private fun explainStoragePermission() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.yacv_needs_storage)
            .setMessage(R.string.yacv_explain_storage)
            .setPositiveButton(R.string.got_it) { _, _ -> }
        builder.create()
    }
}