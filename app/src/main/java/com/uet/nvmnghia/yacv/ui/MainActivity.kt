package com.uet.nvmnghia.yacv.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavInflater
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.uet.nvmnghia.yacv.R
import dagger.hilt.android.AndroidEntryPoint


// Inject dependency
// https://developer.android.com/training/dependency-injection/hilt-android#android-classes
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var mNavController: NavController

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

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

        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment)

        val navInflater: NavInflater = mNavController.navInflater
        val graph = navInflater.inflate(R.navigation.main_graph)

        val isReadPermissionGranted = checkReadPermissionGranted()

        toggleActionBar(isReadPermissionGranted)
        if (isReadPermissionGranted) {
            setupAppBarAndDrawer()
        } else {
//            graph.startDestination = R.id.permissionFragment
        }

        mNavController.graph = graph
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /**
     * Check if [Manifest.permission.READ_EXTERNAL_STORAGE] is granted.
     */
    private fun checkReadPermissionGranted(): Boolean =
        PackageManager.PERMISSION_GRANTED == ContextCompat
            .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

    /**
     * Setup app bar and drawer.
     */
    private fun setupAppBarAndDrawer() {
        // Passing each menu ID as a set of IDs because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_fragment_about, R.id.nav_fragment_browse_file, R.id.nav_fragment_library)
            .setOpenableLayout(drawerLayout)
            .build()

        // Move Toolbar into fragments, instead of a fixed one here
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(navView, mNavController)
    }

    /**
     * Show or hide action bar. Also change status bar color appropriately.
     *
     * By default status bar color is colorPrimaryVariant. When the action bar
     * is hidden, it doesn't really match the background color. Changing to a
     * theme without action bar seems can't be done. That's why the app theme
     * has statusBarColorWhenNoActionBar attribute, and the status bar color
     * is changed to that attribute when needed.
     */
    private fun toggleActionBar(show: Boolean) {
        if (show) supportActionBar?.show()
        else      supportActionBar?.hide()

        val attrID =
            if (show) R.attr.colorPrimaryVariant
            else R.attr.statusBarColorWhenNoActionBar
        val attrs = theme.obtainStyledAttributes(R.style.Theme_Yacv, intArrayOf(attrID))
        window.statusBarColor = attrs.getColor(0, 0)
        attrs.recycle()
    }

}
