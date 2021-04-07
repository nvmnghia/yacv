package com.uet.nvmnghia.yacv.ui.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.ui.MainActivity
import com.uet.nvmnghia.yacv.utils.Constants
import com.uet.nvmnghia.yacv.ui.permission.PermissionViewModel.ReadPermissionState


/**
 * Educate and ask permission upfront.
 * TODO: Make this a fragment in main_graph, right after
 *   LibraryFragment, and cycles back to it if granted.
 *   The hard part is making the ActionBar goes away,
 *   while matches the status bar color with the background.
 */
class PermissionActivity : AppCompatActivity() {

    val viewModel: PermissionViewModel by viewModels()

    private lateinit var sharedPref: SharedPreferences

    private lateinit var exitTxt:  TextView
    private lateinit var allowTxt: TextView

    private lateinit var requestReadPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var appSettingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var appSettingsIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        sharedPref = getSharedPreferences(
            resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // Launchers have to be setup before onResume()
        setupActivityLaunchers()

        exitTxt = findViewById(R.id.permission_exit)
        exitTxt.setOnClickListener { exit() }

        allowTxt = findViewById(R.id.permission_allow)

        viewModel.readPermissionState.observe(this) { state -> updateView(state) }

        setInitialState()
    }

    /**
     * Set state for [viewModel].
     */
    private fun setInitialState() {
        val state = when {
            isReadPermissionGranted() -> ReadPermissionState.GRANTED
            isReadPermissionDeniedForever() -> ReadPermissionState.DENIED_FOREVER
            else -> ReadPermissionState.NOT_GRANTED
        }
        viewModel.setState(state)
    }

    /**
     * Update UI elements to match states.
     */
    private fun updateView(state: ReadPermissionState) =
        when (state) {
            ReadPermissionState.GRANTED -> {
                if (viewModel.previousState == ReadPermissionState.DENIED_FOREVER) {
                    // This set is needed only if the previous state is DENIED_FOREVER
                    setReadPermissionDeniedForever(false)
                }

                // If the current state is initial, disable transition
                moveToMainActivity(viewModel.previousState != null)
            }
            ReadPermissionState.NOT_GRANTED -> {
                allowTxt.text = resources.getText(R.string.allow_yacv)
                allowTxt.setOnClickListener { launchRequestReadPermission() }
            }
            ReadPermissionState.DENIED_FOREVER -> {
                if (viewModel.previousState != null) {
                    // This set is needed only if the current state is NOT initial
                    setReadPermissionDeniedForever(true)
                }

                allowTxt.text = resources.getString(R.string.settings)
                allowTxt.setOnClickListener { launchAppSettings() }
            }
        }

    /**
     * Check if [Manifest.permission.READ_EXTERNAL_STORAGE] is denied forever.
     */
    private fun isReadPermissionDeniedForever(): Boolean =
        sharedPref.getBoolean(Constants.SHPREF_READ_PERMISSION_DENIED_FOREVER, false)

    /**
     * Now that [Manifest.permission.READ_EXTERNAL_STORAGE] is denied forever.
     */
    private fun setReadPermissionDeniedForever(deniedForever: Boolean) =
        with(sharedPref.edit()) {
            putBoolean(Constants.SHPREF_READ_PERMISSION_DENIED_FOREVER, deniedForever)
            apply()
        }

    /**
     * Check if [Manifest.permission.READ_EXTERNAL_STORAGE] is granted.
     */
    private fun isReadPermissionGranted(): Boolean =
        PackageManager.PERMISSION_GRANTED == ContextCompat
            .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

    /**
     * Ask for [Manifest.permission.READ_EXTERNAL_STORAGE] permission.
     */
    private fun launchRequestReadPermission() =
        requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

    /**
     * Callback to handle [Manifest.permission.READ_EXTERNAL_STORAGE] request result.
     * If [granted] is set, move to [com.uet.nvmnghia.yacv.ui.MainActivity].
     */
    private fun handleRequestReadPermissionResult(granted: Boolean) {
        val isDeniedForever = { ! ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.READ_EXTERNAL_STORAGE) }

        when {
            granted -> viewModel.setState(ReadPermissionState.GRANTED)
            isDeniedForever() -> viewModel.setState(ReadPermissionState.DENIED_FOREVER)
            else -> Constants.pass    // Normal deny, so nothing changes
        }
    }

    /**
     * Launch app settings.
     */
    private fun launchAppSettings() {
        if (!this::appSettingsIntent.isInitialized) {
            appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            appSettingsIntent.data = Uri.fromParts(
                "package", this.packageName, null)
        }

        appSettingsLauncher.launch(appSettingsIntent)
    }

    /**
     * Exit the app.
     */
    private fun exit() = this.finishAffinity()

    /**
     * Move to [com.uet.nvmnghia.yacv.ui.MainActivity], which in turn launches
     * [com.uet.nvmnghia.yacv.ui.library.LibraryFragment], AND pop backstack
     * to avoid going back here.
     */
    private fun moveToMainActivity(animated: Boolean = true) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        if (! animated) overridePendingTransition(0, 0)

        finishAffinity()
        if (! animated) overridePendingTransition(0, 0)
    }

    /**
     * Setup activity launchers:
     * - [requestReadPermissionLauncher], and
     * - [appSettingsLauncher]
     */
    private fun setupActivityLaunchers() {
        requestReadPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission())
            { granted -> handleRequestReadPermissionResult(granted) }

        appSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
            { handleRequestReadPermissionResult(isReadPermissionGranted()) }
    }

}