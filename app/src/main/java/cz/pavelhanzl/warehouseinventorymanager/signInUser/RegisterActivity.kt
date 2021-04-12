package cz.pavelhanzl.warehouseinventorymanager.signInUser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.ActivityRegisterBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

/**
 * Register activity
 *
 * @constructor Create empty Register activity
 */
class RegisterActivity : AppCompatActivity() {
    lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN) //Skryje status bar pro tuto aktivitu

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Registruje viewmodel k danému view
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        //nastaví Databinding a propojí viewmodel s xml
        DataBindingUtil.setContentView<ActivityRegisterBinding>(this, R.layout.activity_register)
            .apply {
                this.setLifecycleOwner(this@RegisterActivity)
                this.viewmodel = registerViewModel
            }

        ActivityRegister_login.setOnClickListener {
            val loginActivityIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(loginActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        registerViewModel.status.observe(this, Observer { status ->
            if (status != "") {
                Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        registerViewModel.moveToDashboard.observe(this, Observer {
            if (it) {
                Toast.makeText(this, R.string.RegistrationSuccess, Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        })

        // přesměruje na stránku s podmínkami užití
        tv_RegisterActivity_Terms_link.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PROJECTS_TERMS_AND_CONDITIONS_URL))
            startActivity(intent)
        }

    }

}