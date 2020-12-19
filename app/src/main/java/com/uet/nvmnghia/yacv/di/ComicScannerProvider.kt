package com.uet.nvmnghia.yacv.di

import android.content.Context
import com.uet.nvmnghia.yacv.parser.ComicScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ComicScannerProvider {

    @Singleton
    @Provides
    fun provideComicScanner(
        @ApplicationContext appContext: Context
    ): ComicScanner {
        return ComicScanner(appContext)
    }

}