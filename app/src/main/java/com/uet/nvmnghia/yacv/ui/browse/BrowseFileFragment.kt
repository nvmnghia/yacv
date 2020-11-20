package com.uet.nvmnghia.yacv.ui.browse

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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