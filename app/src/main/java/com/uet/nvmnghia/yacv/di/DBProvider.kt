package com.uet.nvmnghia.yacv.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.comic.ComicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// https://developer.android.com/training/dependency-injection/hilt-android#hilt-modules
// When @Inject is not enough (abstract class, interface, non-project class), @Module is needed
//
@Module
@InstallIn(SingletonComponent::class)
class DBProvider {
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext    // Hilt container is just an object. If needed, it can be injected like this. TODO: What exactly is a "container"?
        appContext: Context): AppDatabase {
        return Room
            .databaseBuilder(appContext, AppDatabase::class.java, "app_db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Singleton
    @Provides
    fun provideComicDao(db: AppDatabase): ComicDao {
        return db.comicDao()
    }
}