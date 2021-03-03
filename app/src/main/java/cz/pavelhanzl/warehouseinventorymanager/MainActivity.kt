package cz.pavelhanzl.warehouseinventorymanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_header.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

//    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var listener: NavController.OnDestinationChangedListener

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

        //drawerHeaderEmail.text="ee"
        setIndividualMenuItems()


//        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()

    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (toggle.onOptionsItemSelected(item)) {
//            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                Toast.makeText(applicationContext, "Otevreno", Toast.LENGTH_SHORT).show()
//                toggle.syncState()
//                return true
//            } else {
//                Toast.makeText(applicationContext, "Zavreno", Toast.LENGTH_SHORT).show()
//                toggle.syncState()
//                return super.onOptionsItemSelected(item)
//            }
//
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun setUpActionBarBasedOnNavigation() {
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(setOf(//nastaví u kterých destinací (fragmentů a aktivit) se má u drawer menu zobrazovat hamburger ikona
            R.id.dashboardFragment,
            R.id.settingsFragment
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    //Nastaví jednotlivé tlačítka v Drawer Menu, která nejsou součástí navigation component např. logout button
    private fun setIndividualMenuItems() {

        //Logout menu item
        val menuItemLogout = drawerNavigationView.menu.findItem(R.id.menuItem_logout)
        menuItemLogout.setOnMenuItemClickListener {
            Toast.makeText(applicationContext, getString(R.string.LoggingOut), Toast.LENGTH_SHORT).show()
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