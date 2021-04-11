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
import com.google.firebase.firestore.FirebaseFirestore
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentChangeNameBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.signInUser.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_header.*
import kotlinx.android.synthetic.main.menu_header.view.*
import kotlinx.android.synthetic.main.rv_warehouse_people_list_item.view.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration


    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //Registruje viewmodel k danému view
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setUpDrawerMenu()
        setUpActionBarBasedOnNavigation()


    }

    private fun setUpDrawerMenu() {
        navController = findNavController(R.id.fragment)
        drawerLayout = drawer_layout
        drawerNavigationView.setupWithNavController(navController)

        setUpProfileInfoInHeaderOfDrawer()
        setIndividualMenuItems()


    }

    private fun setUpProfileInfoInHeaderOfDrawer() {
        val navigationView = drawerNavigationView as NavigationView
        val headerView = navigationView.getHeaderView(0)

        mainActivityViewModel.name.observe(this, Observer { name ->
            headerView.drawerHeaderEmail.text = name
        })

        mainActivityViewModel.profilePhotoUrl.observe(this, Observer { profilePhotoURL ->
            Glide.with(applicationContext)
                .load(profilePhotoURL)
                .placeholder(R.drawable.avatar_profileavatar)
                .error(R.drawable.avatar_profileavatar)
                .into(profileImage)
        })

    }

    private fun setUpActionBarBasedOnNavigation() {
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(//nastaví u kterých destinací (fragmentů a aktivit) se má u drawer menu zobrazovat hamburger ikona
                R.id.dashboardFragment,
                R.id.settingsFragment,
                R.id.listOfWarehousesFragment,
                R.id.invitationsFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    //Nastaví jednotlivé tlačítka v Drawer Menu, která nejsou součástí navigation component např. logout button
    private fun setIndividualMenuItems() {


        //Logout menu item
        val menuItemLogout = drawerNavigationView.menu.findItem(R.id.menuItem_logout)
        menuItemLogout.setOnMenuItemClickListener {
            logOut()
            true
        }


        //About app menu item
        val menuItemAboutApp = drawerNavigationView.menu.findItem(R.id.menuItem_aboutApp)
        menuItemAboutApp.setOnMenuItemClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.seznam.cz/"))
            startActivity(intent)
            true
        }

        //sem přijdou další tlačítka
    }

    private fun logOut() {
        Toast.makeText(applicationContext, getString(R.string.LoggingOut), Toast.LENGTH_SHORT)
            .show()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard(this)

        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showLoading() {
       Log.d("LOADING OVERLAY", "Ukazuju loading overlay")
       window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
       loadingOverlay.visibility = View.VISIBLE
    }

    fun hideLoading() {
        Log.d("LOADING OVERLAY", "Skrývám loading overlay")
        loadingOverlay.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}