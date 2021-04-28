package com.uet.nvmnghia.yacv.ui.permission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PermissionViewModel @Inject constructor() : ViewModel() {

    private val _readPermissionState = MutableLiveData<ReadPermissionState>()
    val readPermissionState: LiveData<ReadPermissionState>
        get() = _readPermissionState

    var previousState: ReadPermissionState? = null

    fun setState(state: ReadPermissionState) {
        previousState = _readPermissionState.value
        _readPermissionState.postValue(state)
    }

    enum class ReadPermissionState {
        GRANTED,
        NOT_GRANTED,      // Or deny without Never ask again
        DENIED_FOREVER    // Denied with Never ask again, which requires Settings to grant
    }

}