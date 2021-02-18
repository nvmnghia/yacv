package com.uet.nvmnghia.yacv.ui.search

import android.app.SearchManager
import android.content.Intent
import android.util.Log
import com.uet.nvmnghia.yacv.ui.MainActivity
import kotlinx.coroutines.*


/**
 * Given a [searchIntent] whose action is [Intent.ACTION_SEARCH], search the term queried.
 */
fun MainActivity.handleSearch(searchIntent: Intent) {
    searchIntent.getStringExtra(SearchManager.QUERY)?.also { query ->
        CoroutineScope(Dispatchers.IO).launch {
            val matches = searchHandler.search("hulk", preview = true)
            delay(2000)
            withContext(Dispatchers.Main + Job()) {
                matches.value?.flatten()?.forEach { match ->
                    Log.d("yacvsearch", match.getLabel())
                }
            }
        }
    }
}
