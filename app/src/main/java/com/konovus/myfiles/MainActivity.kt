package com.konovus.myfiles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.tabs.TabLayout
import com.konovus.myfiles.ui.categoryFilesScreen.CategoryFiles.Companion.DELETE_REQUEST_CODE
import com.konovus.myfiles.util.onTabSelectedMyListener
import dagger.hilt.android.AndroidEntryPoint


const val TAG = "MyFiles"
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    companion object{
        lateinit var tabLayout: TabLayout
        lateinit var mFragmentManager: FragmentManager
        lateinit var activityInstance: Activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityInstance = this
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        mFragmentManager = supportFragmentManager
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHost.findNavController()

        // Here every fragment id passed is going to be set as a top level destination, without the back arrow
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.mainFragment, R.id.storageExplorerFragment ))
        setupActionBarWithNavController(navController, appBarConfiguration)

        tabLayout = findViewById(R.id.tab_layout)
        tabLayout.onTabSelectedMyListener { tab ->
            when(tab?.position) {
                0 -> {
                    Log.i(TAG, "tabs: categories")
                    navController.navigate(R.id.mainFragment)
                }
                1 -> {
                    Log.i(TAG, "tabs: storage")
                    navController.navigate(R.id.storageExplorerFragment)
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DELETE_REQUEST_CODE) {
            if (resultCode != 0)
                Log.i(TAG, "onActivityResult: File deleted successfully")
        }
    }



}
