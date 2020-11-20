package com.uet.nvmnghia.yacv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var mNavController: NavController

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
        // is handled in nav graph xml, simply by:
        // menu item ID = nav item ID
        // These 2 IDs MUST NOT be the same as the ID of the fragment itself.
        // If the ID of the fragment itself is passed, hamburger will be broken.

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_fragment_about, R.id.nav_fragment_browse_file, R.id.nav_fragment_library)
            .setOpenableLayout(drawerLayout)
            .build()
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(navView, mNavController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
                || super.onSupportNavigateUp()
    }
}