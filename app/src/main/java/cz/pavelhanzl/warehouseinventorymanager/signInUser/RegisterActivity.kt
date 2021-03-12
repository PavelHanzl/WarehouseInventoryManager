package cz.pavelhanzl.warehouseinventorymanager.signInUser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.ActivityRegisterBinding
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide() //Skryje action bar pro tuto aktivitu
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Registruje viewmodel k danému view
        val registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        DataBindingUtil.setContentView<ActivityRegisterBinding>(this, R.layout.activity_register)
            .apply {
                this.setLifecycleOwner(
                    this@RegisterActivity
                )
                this.viewmodel = registerViewModel
            }

        ActivityRegister_login.setOnClickListener {
            val loginActivityIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(loginActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        registerViewModel.status.observe(this, Observer { status ->
            Log.d("String",status.toString() + " ! " + stringResource(R.string.RegistrationSuccess))
            if (status.toString() == stringResource(R.string.RegistrationSuccess)) {
                Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else if (status!=""){
                Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()
            }
        })

    }

}