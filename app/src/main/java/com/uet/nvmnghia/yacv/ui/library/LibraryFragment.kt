package com.uet.nvmnghia.yacv.ui.library


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.comic.ComicDao
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

    lateinit var folderAdapter: FolderAdapter

    lateinit var glide: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Inject this shit
        glide = Glide.with(this)

        folderAdapter = FolderAdapter(glide, comicDao)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        val listComicFolders: RecyclerView = view.findViewById(R.id.library_list_folders)
        listComicFolders.adapter = folderAdapter
        listComicFolders.layoutManager = LinearLayoutManager(activity)
        listComicFolders.setHasFixedSize(true)

        viewModel.folders.observe(viewLifecycleOwner, folderAdapter::submitList)
        askReadExternalThenRescan()

        return view
    }

    private fun askReadExternalThenRescan() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
            }
            else -> {
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { result ->
                    if (result) viewModel.rescanComics()
                }
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}