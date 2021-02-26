package cz.pavelhanzl.warehouseinventorymanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import org.w3c.dom.Text

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide() //Skryje action bar pro tuto aktivitu
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ActivityRegister_RegisterButton.setOnClickListener{
            AtempRegistration()
        }

        ActivityRegister_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    /**
     * Pokusí se registrovat uživatele se zadanými hodnotami, získanými z edittextviews. Před registrací
     * probíhá krátká kontrola zadaných údajů na validitu.
     */
    private fun AtempRegistration() {
        val email = ActivityRegister_emailInput.text.trim().toString()
        val password = ActivityRegister_passwordInput.text.trim().toString()
        val passwordCheck = ActivityRegister_passwordInputCheck.text.trim().toString()

        var cancelRegistration = false
        var focusView = ActivityRegister_emailInput

        //Validace zadaných polí
        if (isEmpty(email)) {
            Toast.makeText(this, getString(R.string.emptyEmail), Toast.LENGTH_SHORT).show()
            focusView=ActivityRegister_emailInput
            cancelRegistration=true
        } else if (isEmpty(password)) {
            Toast.makeText(this, getString(R.string.emptyPassword), Toast.LENGTH_SHORT).show()
            focusView=ActivityRegister_passwordInput
            cancelRegistration=true
        } else if (isEmpty(passwordCheck)) {
            Toast.makeText(this, getString(R.string.emptyEmailCheck), Toast.LENGTH_SHORT).show()
            focusView=ActivityRegister_passwordInputCheck
            cancelRegistration=true
        } else if (!isEmailValid(email)){
            Toast.makeText(this, getString(R.string.invalidEmailFormat), Toast.LENGTH_SHORT).show()
            focusView=ActivityRegister_emailInput
            cancelRegistration=true
        } else if (!isPasswordValid(password,passwordCheck)) {
            Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show()
            focusView=ActivityRegister_passwordInputCheck
            cancelRegistration=true
        }

        if(cancelRegistration){
            focusView.requestFocus() //pokud nastala chyba, tak focusne na první špatně vyplněné pole
        }else{
            createAccount(email,password) //pokud vše bez chyby, tak přejde na registraci
        }
    }

    //ověřuje, že se zadaná hesla rovnají a že je zadané heslo alespoň 6 znaků dlouhé
    private fun isPasswordValid(password: String, passwordCheck: String): Boolean {
        return password.equals(passwordCheck) && password.length >= 6
    }

    //Ověřuje, zdali je parametr prázdný
    private fun isEmpty(input: String) = input.isEmpty()

    //Ověřuje, zdali parametr obsahuje zavináč a tečku
    private fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    //Vytvoří účet pokud email a heslo projdou přes validaci
    private fun createAccount(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(
            OnCompleteListener<AuthResult> { task ->
                if(task.isSuccessful){ // Registrace proběhla úspěšně
                    val firebaseUser = task.result!!.user!!
                    Toast.makeText(this, getString(R.string.RegistrationSuccessful), Toast.LENGTH_SHORT).show()
                } else { // Registrace neproběhla úspěšně
                    Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                }

            }
        )
    }
}