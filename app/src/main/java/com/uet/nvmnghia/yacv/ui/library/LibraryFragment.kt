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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LibraryFragment : Fragment() {

    // Injected field can't be private
    // @Inject in field declaration:       inject something into this field
    // @Inject in constructor declaration: inject this class somewhere, init by this constructor
//    @Inject
//    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewModel: LibraryViewModel by viewModels()

    lateinit var folderAdapter: FolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        folderAdapter = FolderAdapter()

        // Use ViewModelProvider to create ViewModel
        // https://developer.android.com/codelabs/kotlin-android-training-view-model#4
        // ViewModel must be accessed in attached mode
//        viewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)
        // However, we want to use Hilt for DI
        // Therefore the above method doesn't work anymore
        // The correct way is the current implementation outside this function

        viewModel.comics.observe(viewLifecycleOwner, folderAdapter::submitList)
        askReadExternalThenRescan()

        val listComicFolders: RecyclerView = view.findViewById(R.id.library_list_folders)
        listComicFolders.adapter = folderAdapter
        listComicFolders.layoutManager = LinearLayoutManager(activity)
        listComicFolders.setHasFixedSize(true)
        requireActivity()
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