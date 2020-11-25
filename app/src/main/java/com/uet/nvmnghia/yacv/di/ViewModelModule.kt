package com.uet.nvmnghia.yacv.di
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.uet.nvmnghia.yacv.ui.library.LibraryViewModel
//import com.uet.nvmnghia.yacv.ui.viewmodelfactory.ComicViewModelFactory
//import dagger.Binds
//import dagger.Module
//import dagger.multibindings.IntoMap
//
//
//@Module
//abstract class ViewModelModule {
//    @Binds
//    @IntoMap
//    @ViewModelKey(LibraryViewModel::class)
//    abstract fun bindUserViewModel(userViewModel: LibraryViewModel): ViewModel
//
//    @Binds
//    abstract fun bindViewModelFactory(factory: ComicViewModelFactory): ViewModelProvider.Factory
//}