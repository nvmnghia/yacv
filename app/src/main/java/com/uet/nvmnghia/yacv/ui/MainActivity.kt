package com.uet.nvmnghia.yacv.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.AppDatabase
import com.uet.nvmnghia.yacv.model.search.MetadataSearchHandler
import com.uet.nvmnghia.yacv.ui.search.handleSearch
import dagger.hilt.android.AndroidEntryPoint
import java.util.logging.Logger
import javax.inject.Inject


// Inject dependency
// https://developer.android.com/training/dependency-injection/hilt-android#android-classes
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var mNavController: NavController

    @Inject lateinit var searchHandler: MetadataSearchHandler    // For the extension function in SearchActivity.kt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        // Normally, each item of every menu need to be explicitly handled.
        // For example, in DrawerLayout:
        // navView.setNavigationItemSelectedListener(item -> {})
        // in which fragments are launched accordingly.
        // However, with Navigation Component, mapping from item click to fragment
        // is handled in nav graph xml, simply by making:
        // menu item ID = nav item ID
        // The menu is still/must be bound with navView by app:menu in navView's xml.
        // These 2 IDs MUST NOT be the same as the ID of the fragment itself,
        // or else hamburger will be broken.

        // Passing each menu ID as a set of IDs because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_fragment_about, R.id.nav_fragment_browse_file, R.id.nav_fragment_library)
            .setOpenableLayout(drawerLayout)
            .build()

        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment)
        // Move Toolbar into fragments, instead of a fixed one here
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(navView, mNavController)

        if (intent.action == Intent.ACTION_SEARCH) {
            handleSearch(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // MainActivity launch mode is singleTop, so search intent is also checked here
        if (intent?.action == Intent.ACTION_SEARCH) {
            handleSearch(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
                || super.onSupportNavigateUp()
    }
}