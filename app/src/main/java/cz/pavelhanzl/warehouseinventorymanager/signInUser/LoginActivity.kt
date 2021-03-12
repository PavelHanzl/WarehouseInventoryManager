package cz.pavelhanzl.warehouseinventorymanager.signInUser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.ActivityLoginBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.ActivityRegisterBinding
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*


class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        supportActionBar?.hide() //Skryje action bar pro tuto aktivitu

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Registruje viewmodel k danému view
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        //nastaví Databinding a propojí viewmodel s xml
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
            .apply {
                this.setLifecycleOwner(this@LoginActivity)
                this.viewmodel = loginViewModel
            }


        // Configure Google Sign In
        setGoogleSignIn()

        //Pokusí se přihlásit pomcí googlu
        google_sign_in_button.setOnClickListener {
            signIn()
        }


        loginViewModel.status.observe(this, Observer { status ->
            if (status != "") {
                Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        loginViewModel.moveToDashboard.observe(this, Observer {
            if (it) {
                Toast.makeText(this, R.string.LoginSuccessful, Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        })

        //Pokusí se přihlásit emailem a heslem
        ActivityLogin_loginButton.setOnClickListener() {
            //attempLoginWithEmailAndPassword()
        }

        //Přechází na aktivitu s registrací
        ActivityLogin_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

    }

    private fun setGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
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
            if (task.isSuccessful) {
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
            } else {
                Log.w("SignInWithGoogle", exception.toString())
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Přihlášení pomocí googlu proběhlo úspěšně
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, getString(R.string.LoginSuccessful), Toast.LENGTH_SHORT)
                        .show()
                    val intentMainActivity = Intent(this, MainActivity::class.java)
                    startActivity(intentMainActivity)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else { // Přihlášení pomocí googlu neproběhlo úspěšně
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

}