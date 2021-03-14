package cz.pavelhanzl.warehouseinventorymanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import cz.pavelhanzl.warehouseinventorymanager.signInUser.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_header.*
import kotlinx.android.synthetic.main.menu_header.view.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //Registruje viewmodel k danému view
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //Skryje status bar pro tuto aktivitu

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

        mainActivityViewModel.profilePhoto.observe(this, Observer { profilePhoto ->
            profileImage.setImageBitmap(profilePhoto)
        })

    }

    private fun setUpActionBarBasedOnNavigation() {
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(//nastaví u kterých destinací (fragmentů a aktivit) se má u drawer menu zobrazovat hamburger ikona
                R.id.dashboardFragment,
                R.id.settingsFragment,
                R.id.ownWarehouseFragment
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
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}