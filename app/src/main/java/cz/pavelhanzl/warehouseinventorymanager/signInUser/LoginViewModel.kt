package cz.pavelhanzl.warehouseinventorymanager.signInUser

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.User
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.lang.Exception

class LoginViewModel : BaseViewModel() {
    private val passwordLength = Constants.MIN_PASSWORD_LENGTH

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    var emailContent = MutableLiveData<String>("")
    var passwordContent = MutableLiveData<String>("")

    private val _emailError = MutableLiveData<String>("")
    val emailError: LiveData<String> get() = _emailError

    private val _passwordError = MutableLiveData<String>("")
    val passwordError: LiveData<String> get() = _passwordError

    private val _status = MutableLiveData<String>("")
    val status: LiveData<String> get() = _status

    private val _moveToDashboard = MutableLiveData<Boolean>()
    val moveToDashboard: LiveData<Boolean> get() = _moveToDashboard

    /**
     * On login click
     * Perfom login operation, if all inputs are valid
     */
    fun onLoginClick() {
        if (validForLogin()) {
            login(emailContent.value!!, passwordContent.value!!)
        }
    }

    /**
     * Valid for login
     * Check if all given information are correct and valid
     * @return returns true if valid
     */
    fun validForLogin(): Boolean {

        val emailValidation = validateEmail()
        val lengthPasswordValidation = validatePasswordLength()

        return emailValidation && lengthPasswordValidation
    }

    /**
     * Validates password length
     *
     * @return returns true if valid
     */
    private fun validatePasswordLength(): Boolean {
        return if (!(passwordContent.value?.length!! >= passwordLength)) {
            _passwordError.value =
                stringResource(R.string.password_must_have) + passwordLength + stringResource(R.string.number_of_characters)
            false
        } else {
            _passwordError.value = ""
            true
        }
    }

    /**
     * Validates email format
     *
     * @return returns true if valid
     */
    private fun validateEmail(): Boolean {
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailContent.value!!).matches()) {
            _emailError.value = stringResource(R.string.mail_is_not_in_form)
            false
        } else {
            _emailError.value = ""
            true
        }
    }

    /**
     * Validates format of email for forgotten pass
     *
     * @param email email address to validate
     * @return returns true if valid
     */
    fun validateEmailForForgottenPass(email: String): Pair<Boolean, String> {
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Pair(false, stringResource(R.string.mail_is_not_in_form))
        } else {
            return Pair(true, "")
        }
    }

    /**
     * Sends reset password email to  given email
     *
     * @param email email where to send reset password email
     */
    fun sendResetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    /**
     * Login
     * Performs a login based on email and password
     * @param email users email
     * @param password users password
     */
    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(
            OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) { // Přihlášení pomocí emailu a hesla proběhlo úspěšně
                    _moveToDashboard.value = true
                } else { // Přihlášení pomocí emailu a hesla neproběhlo úspěšně
                    _status.value = task.exception!!.message.toString()
                }
            }
        )
    }

    /**
     * Saves user profile photo from google auth
     *
     * @return returns upload task
     */
    fun saveUserProfilePhotoFromGoogleAuth(): UploadTask {
        val userImageURL = auth.currentUser!!.photoUrl.toString()
        var photoRef = storage.child("images/users/" + auth.currentUser!!.uid + "/profile.jpg")

        val picture = Picasso.get().load(userImageURL).get()
        val baos = ByteArrayOutputStream()
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()


        return photoRef.putBytes(data)
    }

    /**
     * Creates user in firebase firestore in users collection
     *
     * @param name name of user
     * @param email users email
     * @return returns task
     */
    fun createUserInFirestore(name: String, email: String): Task<Void> {

        val user: MutableMap<String, Any> = HashMap()
        user["userID"] = auth.currentUser!!.uid
        user["name"] = name
        user["email"] = email
        user["photoURL"] = ""

        return db.collection(Constants.USERS_STRING).document(FirebaseAuth.getInstance().currentUser!!.uid)
            .set(user)
    }

    /**
     * Performs a login based on firebase auth with google - logic
     *
     * @param credential credential with which we want to login
     */
    fun firebaseAuthWithGoogleLogic(credential: AuthCredential) {
        _loading.postValue(true)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                auth.signInWithCredential(credential).await()
                Log.d("Firestore", "Login with google")
            } catch (e: FirebaseAuthException) {
                Log.d("Firestore", "Exception: " + "${e.message}")
                _loading.postValue(false)
                return@launch
            }

            try {
                val userDocumentRef =
                    db.collection(Constants.USERS_STRING).document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .get().await()
                if (userDocumentRef.exists()) {
                    _moveToDashboard.postValue(true)
                    _loading.postValue(false)
                    return@launch
                }
            } catch (e: Exception) {
                Log.d("Firestore", "Exception" + "${e.message}")
                _loading.postValue(false)
            }

            try {
                createUserInFirestore(
                    auth.currentUser!!.displayName.toString(),
                    auth.currentUser!!.email.toString()
                ).await()
            } catch (e: Exception) {
                auth.currentUser!!.delete()
                _loading.postValue(false)
                return@launch
            }

            try {
                val usersDocRef = db.collection(Constants.USERS_STRING).document(auth.currentUser!!.uid)
                val usersDocSnap= usersDocRef.get().await()
                val userObject = usersDocSnap.toObject(User::class.java)

                //pokud user nemá v db uloženou fotku tak to vezme fotku z googlu a uloží ji do databáze
                if (userObject?.photoURL.isNullOrEmpty()) {
                    saveUserProfilePhotoFromGoogleAuth().await()
                    val photoUrl = storage.child("images/users/" + auth.currentUser!!.uid + "/profile.jpg").downloadUrl.await().toString()
                    usersDocRef.update("photoURL", photoUrl)
                }


            } catch (e: Exception) {
                Log.d("Storage", "Exception" + "${e.message}")
            }

            _loading.postValue(false)
            _moveToDashboard.postValue(true)

        }
    }

}

