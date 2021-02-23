package com.uet.nvmnghia.yacv.ui.search

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler


class SearchViewModel @ViewModelInject constructor(
    @Assisted savedStateHandle: SavedStateHandle,    // Access Fragment/Activity args
    application: Application,
) : ViewModel() {

    val query: String = savedStateHandle.get<String>(application.resources.getString(R.string.query))
        ?: throw IllegalArgumentException("Missing query when instantiating SearchFragment")

}