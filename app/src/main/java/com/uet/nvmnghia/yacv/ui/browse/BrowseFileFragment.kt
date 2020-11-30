package com.uet.nvmnghia.yacv.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.uet.nvmnghia.yacv.R

class BrowseFileFragment : Fragment() {

    companion object {
        fun newInstance() = BrowseFileFragment()
    }

    private lateinit var viewModel: BrowseFileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse_file, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BrowseFileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}