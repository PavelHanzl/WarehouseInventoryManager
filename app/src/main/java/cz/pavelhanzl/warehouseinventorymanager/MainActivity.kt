package cz.pavelhanzl.warehouseinventorymanager

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.signInUser.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.menu_header.*
import kotlinx.android.synthetic.main.menu_header.view.*
import kotlinx.android.synthetic.main.rv_warehouse_people_list_item.view.*
import kotlinx.coroutines.*

/**
 * Main activity
 * Main activity of the application.
 * @constructor Create empty Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //Registruje viewmodel k danému view
        mainActivityViewModel =
            ViewModelProvider(this).get(MainActivityViewModel::class.java)



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setUpDrawerMenu()
        setUpActionBarBasedOnNavigation()


    }

    /**
     * Sets up drawer menu
     */
    private fun setUpDrawerMenu() {
        navController = findNavController(R.id.fragment)
        drawerLayout = drawer_layout
        drawerNavigationView.setupWithNavController(navController)

        setUpProfileInfoInHeaderOfDrawer()
        setIndividualMenuItems()


    }

    /**
     * Sets up profile info in header of drawer
     * Sets up profile photo and users display name
     */
    private fun setUpProfileInfoInHeaderOfDrawer() {
        val navigationView = drawerNavigationView as NavigationView
        val headerView = navigationView.getHeaderView(0)

        mainActivityViewModel.name.observe(this, Observer { name ->
            headerView.drawerHeaderEmail.text = name
        })

        try {
            mainActivityViewModel.profilePhotoUrl.observe(
                this,
                Observer { profilePhotoURL ->
                    Glide.with(applicationContext)
                        .load(profilePhotoURL)
                        .placeholder(R.drawable.avatar_profileavatar)
                        .error(R.drawable.avatar_profileavatar)
                        .into(profileImage)
                })
        } catch (e: Exception) {
            Log.d("Drawer menu header", "Image not ready yet.")
        }

    }

    /**
     * Sets up action bar based on navigation
     * Sets  which destinations should have hamburger menu icon
     */
    private fun setUpActionBarBasedOnNavigation() {
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(//nastaví u kterých destinací (fragmentů a aktivit) se má u drawer menu zobrazovat hamburger ikona
                R.id.dashboardFragment,
                R.id.settingsFragment,
                R.id.listOfWarehousesFragment,
                R.id.invitationsFragment
                //R.id.aboutFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Set individual menu items
     * Sets individual buttons in the Drawer Menu that are not part of the navigation component, eg logout button
     */
    private fun setIndividualMenuItems() {

        //Logout menu item
        val menuItemLogout =
            drawerNavigationView.menu.findItem(R.id.menuItem_logout)
        menuItemLogout.setOnMenuItemClickListener {
            logOut()
            true
        }

        //About app menu item
        val menuItemAboutApp =
            drawerNavigationView.menu.findItem(R.id.menuItem_aboutApp)

        //Sets on click listener for About app menu item
        menuItemAboutApp.setOnMenuItemClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.PROJECTS_WEBSITE_URL)
            )
            startActivity(intent)
            true
        }

        //Contact developer app menu item
        val menuItemContact =
            drawerNavigationView.menu.findItem(R.id.menuItem_contact)

        menuItemContact.setOnMenuItemClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.PROJECTS_WEBSITE_URL_CONTACT)
            )
            startActivity(intent)
            true
        }

        //sem mohou přijít další tlačítka se speciální funkcionalitou
    }

    /**
     * Performs Log out
     */
    private fun logOut() {
        //shows short pop up message informing about logging out
        Toast.makeText(
            applicationContext,
            getString(R.string.LoggingOut),
            Toast.LENGTH_SHORT
        ).show()

        //sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        //starts new activity - LoginActivity in this case
        startActivity(
            Intent(
                this@MainActivity,
                LoginActivity::class.java
            )
        )

        //displays a nicer transition animation
        //than is the default transition animation
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        //finishes this activity, so user can not use back button of device
        //to go back to this location - MainActivity in this case
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard(this)

        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Shows loading overlay
     *
     */
    fun showLoadingOverlay() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        loadingOverlay.visibility = View.VISIBLE
    }

    /**
     * Hides loading overlay
     *
     */
    fun hideLoadingOverlay() {
        loadingOverlay.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


}