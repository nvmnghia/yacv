package com.uet.nvmnghia.yacv.model.comic

import androidx.lifecycle.LiveData
import com.uet.nvmnghia.yacv.parser.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


// An object instance managed by Hilt needs to be stored in a container
// like bean & IoC container in Spring
// This annotation scopes an instance of DBProvider to application container
// therefore that exact & only instance is provided anywhere needed.
// Of course there're more scoping annotations
@Singleton
class ComicRepository @Inject constructor(
//    private val executor: Executor,
    private val comicDao: ComicDao
) {
    fun getComics(rescan: Boolean = false): LiveData<List<Comic>> {
        if (rescan) rescanComics()
        return comicDao.load()
    }

    fun rescanComics() {
        // Run in background
//        executor.execute {
//            comicDao.save(Scanner.scan().map { comicFile -> Comic(comicFile.path.toString()) })
//        }
        CoroutineScope(Dispatchers.IO).launch {
            comicDao.save(Scanner.scan().map { comicFile -> Comic(comicFile.path.toString()) })
        }
    }
}