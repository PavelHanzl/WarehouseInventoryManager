package cz.pavelhanzl.warehouseinventorymanager

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //Vypne defaultně tmavý režim při spuštění
        supportActionBar?.hide() //Skryje action bar pro tuto aktivitu

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ActivityLogin_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }

}