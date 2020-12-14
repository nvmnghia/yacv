package com.uet.nvmnghia.yacv.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.author.RoleTable
import com.uet.nvmnghia.yacv.model.comic.ComicDao
import com.uet.nvmnghia.yacv.model.folder.FolderDao
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

    val rdc = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            RoleTable.populate(db)
        }
    }

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext    // Hilt container is just an object. If needed, it can be injected like this. TODO: What exactly is a "container"?
        appContext: Context,
    ): AppDatabase {
        return Room
            .databaseBuilder(appContext, AppDatabase::class.java, "app_db")
            .addCallback(rdc)
            .build()
    }

    @Singleton
    @Provides
    fun provideComicDao(db: AppDatabase): ComicDao {
        return db.comicDao()
    }

    @Singleton
    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao {
        return db.folderDao()
    }
}