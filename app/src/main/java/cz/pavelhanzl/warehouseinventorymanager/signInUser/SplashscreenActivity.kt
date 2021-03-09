package cz.pavelhanzl.warehouseinventorymanager.signInUser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R

class SplashscreenActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //Vypne defaultně tmavý režim při spuštění pro celou apku
        supportActionBar?.hide() //Skryje action bar pro tuto aktivitu

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        //zobrazí splashscreen po dobu 0,7 sekundy
        Handler(Looper.getMainLooper()).postDelayed({
            checkIfAlreadyLoggedIn()
        }, 1570)

    }

    /**
     * Kontroluje jestli je uživatel již přihlášen, pokud ano, tak přesměrovává na hlavní aktivitu.
     */
    private fun checkIfAlreadyLoggedIn() {
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

        if (user != null) { //pokud je někdo přihlášen, tak přesměruje do hlavní aktivity
            val mainActivityIntent = Intent(this@SplashscreenActivity, MainActivity::class.java)
            startActivity(mainActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        } else { //pokud nění někdo přihlášen, tak přesměruje na loginActivitu
            val loginActivityIntent = Intent(this@SplashscreenActivity, LoginActivity::class.java)
            startActivity(loginActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }


}