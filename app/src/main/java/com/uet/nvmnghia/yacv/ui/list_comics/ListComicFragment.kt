package com.uet.nvmnghia.yacv.ui.list_comics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.uet.nvmnghia.yacv.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ListComicFragment: Fragment() {

    val viewModel: ListComicViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse_folder, container, false)
    }

}