package com.uet.nvmnghia.yacv.di

import android.content.Context
import com.uet.nvmnghia.yacv.parser.ComicScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped


@Module
@InstallIn(FragmentComponent::class)
class ComicScannerProvider {

    @FragmentScoped
    @Provides
    fun provideComicScanner(
        @ApplicationContext applicationContext: Context
    ): ComicScanner {
        return ComicScanner(applicationContext)
    }
}