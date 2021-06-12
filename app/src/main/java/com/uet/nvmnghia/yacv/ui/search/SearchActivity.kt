package com.uet.nvmnghia.yacv.ui.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.search.queryAllTypes
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val queryString: String =
            if (intent.action == Intent.ACTION_SEARCH) intent.getStringExtra(SearchManager.QUERY)!!
            else throw IllegalStateException("SearchActivity only receives Intent.ACTION_SEARCH.")
        val query = queryAllTypes(queryString, true)

        setContentView(R.layout.activity_search)

        val toolbar: Toolbar = findViewById(R.id.toolbar_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val queryWrapper = Bundle()
        queryWrapper.putParcelable("query", query)

        val mNavController = Navigation.findNavController(this, R.id.search_nav_host_fragment)
        mNavController.setGraph(R.navigation.search_graph, queryWrapper)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}