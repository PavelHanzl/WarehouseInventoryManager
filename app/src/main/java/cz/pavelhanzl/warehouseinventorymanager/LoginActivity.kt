package cz.pavelhanzl.warehouseinventorymanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*


class LoginActivity : AppCompatActivity() {

    companion object{
        private const val RC_SIGN_IN = 120
    }
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //Vypne defaultně tmavý režim při spuštění
        supportActionBar?.hide() //Skryje action bar pro tuto aktivitu

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkIfAlreadyLoggedIn()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        google_sign_in_button.setOnClickListener{
            signIn()
        }

        ActivityLogin_loginButton.setOnClickListener() {
            attempLoginWithEmailAndPassword()
        }

        //Přechází na aktivitu s registrací
        ActivityLogin_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }

    /**
     * Kontroluje jestli je uživatel již přihlášen, pokud ano, tak přesměrovává na hlavní aktivitu.
     */
    private fun checkIfAlreadyLoggedIn() {
        val user = mAuth.currentUser

        if (user != null) {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
    }

    private fun attempLoginWithEmailAndPassword() {
        val email = ActivityLogin_emailInput.text.trim().toString()
        val password = ActivityLogin_passwordInput.text.trim().toString()

        var cancelLogin = false
        var focusView = ActivityLogin_emailInput

        //Validace zadaných polí
        if (isEmpty(email)) {
            Toast.makeText(this, getString(R.string.emptyEmail), Toast.LENGTH_SHORT).show()
            focusView = ActivityLogin_emailInput
            cancelLogin = true
        } else if (isEmpty(password)) {
            Toast.makeText(this, getString(R.string.emptyPassword), Toast.LENGTH_SHORT).show()
            focusView = ActivityLogin_passwordInput
            cancelLogin = true
        } else if (!isEmailValid(email)) {
            Toast.makeText(this, getString(R.string.invalidEmailFormat), Toast.LENGTH_SHORT).show()
            focusView = ActivityLogin_emailInput
            cancelLogin = true
        }

        if (cancelLogin) {
            focusView.requestFocus() //pokud nastala chyba, tak focusne na první špatně vyplněné pole
        } else {
            login(email, password) //pokud vše bez chyby, tak přejde na registraci
        }
    }


    //Ověřuje, zdali je parametr prázdný
    private fun isEmpty(input: String) = input.isEmpty()

    //Ověřuje, zdali parametr obsahuje zavináč a tečku
    private fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    //provede login na základě emailu a hesla
    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) { // Přihlášení proběhlo úspěšně
                        val firebaseUser = task.result!!.user!!
                        Toast.makeText(this, getString(R.string.LoginSuccessful), Toast.LENGTH_SHORT).show()

                        // Odstraní activity běžící na pozadí ve stacku, pomocí extra předá user_id a email, přejde na hlavní aktivitu a ukončí tuto aktivitu
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
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

    //google sign in method
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful){
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("SignInWithGoogle", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("SignInWithGoogle", "Google sign in failed", e)
                    // ...
                }
            } else{
                Log.w("SignInWithGoogle", exception.toString())
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignInWithGoogle", "signInWithCredential:success")
                    val intentMainActivity = Intent(this, MainActivity::class.java)
                    startActivity(intentMainActivity)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignInWithGoogle", "signInWithCredential:failure", task.exception)
                }

                // ...
            }
    }

}