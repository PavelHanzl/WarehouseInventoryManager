package cz.pavelhanzl.warehouseinventorymanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import cz.pavelhanzl.warehouseinventorymanager.repository.User
import cz.pavelhanzl.warehouseinventorymanager.signInUser.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_header.*
import kotlinx.android.synthetic.main.menu_header.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpDrawerMenu()
        setUpActionBarBasedOnNavigation()


    }

    private fun setUpDrawerMenu() {
        navController = findNavController(R.id.fragment)
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerNavigationView.setupWithNavController(navController)

        setUpEmailInHeaderOfDrawer()
        setIndividualMenuItems()


    }

    private fun setUpEmailInHeaderOfDrawer() {
        val navigationView = drawerNavigationView as NavigationView
        val headerView = navigationView.getHeaderView(0)

        CoroutineScope(Dispatchers.IO).launch {
            val userDocument =
                db.collection("users").document(mAuth.currentUser!!.uid).get().await()
            val currentlyLoggedInUser = userDocument.toObject<User>()
            withContext(Dispatchers.Main) {
                headerView.drawerHeaderEmail.text = currentlyLoggedInUser!!.name
            }
        }


    }


    private fun setUpActionBarBasedOnNavigation() {
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(//nastaví u kterých destinací (fragmentů a aktivit) se má u drawer menu zobrazovat hamburger ikona
                R.id.dashboardFragment,
                R.id.settingsFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    //Nastaví jednotlivé tlačítka v Drawer Menu, která nejsou součástí navigation component např. logout button
    private fun setIndividualMenuItems() {

        //Logout menu item
        val menuItemLogout = drawerNavigationView.menu.findItem(R.id.menuItem_logout)
        menuItemLogout.setOnMenuItemClickListener {
            Toast.makeText(applicationContext, getString(R.string.LoggingOut), Toast.LENGTH_SHORT)
                .show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
            true
        }

        //sem přijdou další tlačítka
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}