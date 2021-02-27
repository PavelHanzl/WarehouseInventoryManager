package cz.pavelhanzl.warehouseinventorymanager

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //Vypne defaultně tmavý režim při spuštění
        supportActionBar?.hide() //Skryje action bar pro tuto aktivitu

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ActivityLogin_loginButton.setOnClickListener(){
            attempLogin()
        }

        //Přechází na aktivitu s registrací
        ActivityLogin_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }

    private fun attempLogin(){
        val email = ActivityLogin_emailInput.text.trim().toString()
        val password = ActivityLogin_passwordInput.text.trim().toString()

        var cancelLogin = false
        var focusView = ActivityLogin_emailInput

        //Validace zadaných polí
        if (isEmpty(email)) {
            Toast.makeText(this, getString(R.string.emptyEmail), Toast.LENGTH_SHORT).show()
            focusView=ActivityLogin_emailInput
            cancelLogin=true
        } else if (isEmpty(password)) {
            Toast.makeText(this, getString(R.string.emptyPassword), Toast.LENGTH_SHORT).show()
            focusView=ActivityLogin_passwordInput
            cancelLogin=true
        } else if (!isEmailValid(email)){
            Toast.makeText(this, getString(R.string.invalidEmailFormat), Toast.LENGTH_SHORT).show()
            focusView=ActivityLogin_emailInput
            cancelLogin=true
        }

        if(cancelLogin){
            focusView.requestFocus() //pokud nastala chyba, tak focusne na první špatně vyplněné pole
        }else{
            login(email,password) //pokud vše bez chyby, tak přejde na registraci
        }
    }


    //Ověřuje, zdali je parametr prázdný
    private fun isEmpty(input: String) = input.isEmpty()

    //Ověřuje, zdali parametr obsahuje zavináč a tečku
    private fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->
                    if(task.isSuccessful){ // Přihlášení proběhlo úspěšně
                        val firebaseUser = task.result!!.user!!
                        Toast.makeText(this, getString(R.string.LoginSuccessful), Toast.LENGTH_SHORT).show()

                        // Odstraní activity běžící na pozadí ve stacku, pomocí extra předá user_id a email, přejde na hlavní aktivitu a ukončí tuto aktivitu
                        val intent = Intent(this@LoginActivity,MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                        intent.putExtra("email_id", email)
                        startActivity(intent)
                        finish()

                    } else { // Přihlášení neproběhlo úspěšně
                        Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }

                }
        )
    }
}