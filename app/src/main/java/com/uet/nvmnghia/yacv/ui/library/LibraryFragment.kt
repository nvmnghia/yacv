package com.uet.nvmnghia.yacv.ui.library


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.folder.Folder
import com.uet.nvmnghia.yacv.ui.library.LibraryViewModel.TextState.*
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

    /**
     * [RecyclerView] of list comic folders & its adapter.
     */
    lateinit var listComicFolders: RecyclerView
    lateinit var folderAdapter: FolderAdapter
    lateinit var listComicFoldersObserver: Observer<List<Folder>>

    /**
     * [TextView] displayed when [listComicFolders] is empty,
     * showing the reason why there's no list.
     */
    lateinit var noListTextView: TextView

    /**
     * Number of column, set to [calculateNumberOfColumns]'s returned value.
     */
    var NUM_COL: Int? = null

    /**
     * Activity launchers
     */
    lateinit var folderPickerLauncher: ActivityResultLauncher<Uri>
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var appSettingsLauncher: ActivityResultLauncher<Intent>
    lateinit var APP_SETTING_INTENT: Intent


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

        listComicFoldersObserver = Observer { folderAdapter.submitList(it) }
        viewModel.folders.observe(viewLifecycleOwner, listComicFoldersObserver)
        viewModel.textState.observe(viewLifecycleOwner, this::toggleEmptyListText)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Remember to include in onCreateView
        // setHasOptionsMenu(true)
        inflater.inflate(R.menu.library_toolbar, menu)
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
     * When shown, there could be 3 values:
     * - No folder selected
     * - Cannot read folder (possibly deleted)
     * - No read permission
     * - No comic found
     */
    private fun toggleEmptyListText(state: LibraryViewModel.TextState) {
        noListTextView.text = when (state) {
            NO_ROOT_FOLDER -> {
                HandleNoListTextView.noRootFolder(
                    resources, this::changeRootScanFolder)
            }
            CANNOT_READ_ROOT_FOLDER -> {
                HandleNoListTextView.cannotReadRootFolder(
                    resources, this::changeRootScanFolder)
            }
            NO_READ_PERMISSION -> {
                HandleNoListTextView.noReadPermission(
                    resources, this::launchRequestReadPermission)
            }
            NO_READ_PERMISSION_FOREVER -> {
                HandleNoListTextView.noReadPermissionTwice(resources)
                    { appSettingsLauncher.launch(APP_SETTING_INTENT) }
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
        noListTextView.movementMethod = LinkMovementMethod.getInstance()

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
     * Setup [RecyclerView] for list comic folders
     */
    private fun setupListComicFolders(view: View) {
        // General setup
        listComicFolders = view.findViewById(R.id.library_list_folders)
        listComicFolders.adapter = folderAdapter
        listComicFolders.layoutManager = GridLayoutManager(activity, NUM_COL!!)
        listComicFolders.setHasFixedSize(true)

        // Spacing
        val SPACING = resources.getDimension(R.dimen.library_item_folder_spacing).toInt()
        val spacer = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State,
            ) {
                // Trick:
                // - in library_list_folders, pad left and top only
                // - in this spacing code, add right and bottom spacing only
                outRect.right = SPACING
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

        // Text displayed if empty list
        noListTextView = view.findViewById(R.id.library_no_list_info)
    }

    /**
     * Calculate number of column in the [RecyclerView].
     */
    private fun calculateNumberOfColumns(): Int {
        val screenWidth: Int = DeviceUtil.getScreenWidthInPx(requireContext())
        val columnWidth = resources.getDimension(R.dimen.library_item_folder_column_width).toInt()    // In pixel, already scaled.
        val numColumn = screenWidth / columnWidth
        return if (numColumn != 0) numColumn else 1
    }


    //================================================================================
    // Activity result callbacks
    //================================================================================

    private fun registerActivityResultCallbacks() {
        setupFolderPickerLauncher()
        setupRequestPermissionLauncher()
        setupAppSettingsLauncher()
    }

    private fun setupFolderPickerLauncher() {
        folderPickerLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { folderUri ->
            folderUri?.let { viewModel.changeRootFolder(it) }
        }
    }

    private fun setupRequestPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                launchFolderPicker()
            } else {
                // Check if deny with Never ask again
                if (! shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    viewModel.readPermissionNotGrantedForever()
                }
            }
        }
    }

    private fun setupAppSettingsLauncher() {
        APP_SETTING_INTENT = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        APP_SETTING_INTENT.data = Uri.fromParts(
            "package", requireContext().packageName, null)

        appSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            isReadPermissionGranted()
        }
    }


    //================================================================================
    // Storage & scan
    //================================================================================

    /**
     * Check if READ_EXTERNAL_STORAGE is granted.
     * If not granted, call [LibraryViewModel.readPermissionNotGranted].
     */
    private fun isReadPermissionGranted(): Boolean {
        val granted = ContextCompat.checkSelfPermission(requireActivity(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        when (granted) {
            true -> viewModel.readPermissionGranted()
            false -> viewModel.readPermissionNotGranted()
        }

        return granted
    }


    /**
     * Select a root folder. If not granted, ask for READ_EXTERNAL_STORAGE.
     */
    private fun changeRootScanFolder() {
        when {
            isReadPermissionGranted() -> {
                launchFolderPicker()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                explainStoragePermission()
            }
            else -> {
                launchRequestReadPermission()
            }
        }
    }

    /**
     * Show a dialog explaining why yacv needs storage permission.
     */
    private fun explainStoragePermission() {
        val builder = AlertDialog.Builder(requireActivity())
        builder
            .setTitle(R.string.yacv_needs_storage)
            .setMessage(R.string.yacv_explain_storage)
            .setPositiveButton(R.string.ok_allow) { _, _ ->
                launchRequestReadPermission() }
            .setNegativeButton(R.string.deny) { _, _ -> }
        builder.create().show()
    }

    /**
     * Launch folder picker.
     * If a specific location needs to be opened first, pass its URI to the function.
     */
    private fun launchFolderPicker(suggestedUri: Uri? = null) {
        folderPickerLauncher.launch(null)
    }

    /**
     * Ask for READ_EXTERNAL_STORAGE permission
     */
    private fun launchRequestReadPermission() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
