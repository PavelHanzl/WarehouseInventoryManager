package cz.pavelhanzl.warehouseinventorymanager.signInUser

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.WarehouseInventoryManagerApp
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import cz.pavelhanzl.warehouseinventorymanager.repository.MainRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    private val passwordLength = 6
    private val db = FirebaseFirestore.getInstance()

    // proměné, které může měnit View a jsou nabindované přes XML
    val nameContent = MutableLiveData<String>("")
    val emailContent = MutableLiveData<String>("")
    val password1Content = MutableLiveData<String>("")
    val password2Content = MutableLiveData<String>("")

    // pár otevřené (Mutable) a uzavřené (ne Mutable) proměnné, které nemůže měnit view
    private val _nameError = MutableLiveData<String>()
    val nameError: LiveData<String> get() = _nameError

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> get() = _emailError

    private val _passwordErrorLength = MutableLiveData<String>()
    val passwordErrorLength: LiveData<String> get() = _passwordErrorLength

    private val _passwordErrorSimilarity = MutableLiveData<String>()
    val passwordErrorSimilarity: LiveData<String> get() = _passwordErrorSimilarity


    var status = MutableLiveData<String>("")
    ///


    fun onRegisterClick() {

        if (validForRegistration()) {

            viewModelScope.launch(Dispatchers.IO) {
                var coroutineStatus = ""
                var exception: String? = null

                try {
                    val firebase = createUserInFirebaseAuth(
                        emailContent.value!!,
                        password1Content.value!!
                    ).await()
                    Log.d("Fire", firebase.toString())
                } catch (e: Exception) {
                    Log.d("Fire", e.toString())
                    exception = e.localizedMessage
                }

                if (exception == null){
                    createUserInFirestore(nameContent.value!!, emailContent.value!!).await()
                    coroutineStatus = stringResource(R.string.RegistrationSuccess)
                    Log.d("Fire", "Dělam usera")
                } else{
                    coroutineStatus=exception.toString()
                }

                withContext(Dispatchers.Main) {
                    status.value = coroutineStatus
                }
            }


        }
    }


    fun validForRegistration(): Boolean {

        val nameValidation = validateName()
        val emailValidation = validateEmail()
        val samePasswordValidation = validateSamePassword()
        val lengthPasswordValidation = validatePasswordLength()

        return nameValidation && emailValidation && samePasswordValidation && lengthPasswordValidation
    }

    private fun validateName(): Boolean {
        return if (nameContent.value?.isEmpty()!!) {
            _nameError.value = stringResource(R.string.type_in_name)
            false
        } else {
            _nameError.value = ""
            true
        }
    }

    private fun validateEmail(): Boolean {
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailContent.value!!).matches()) {
            _emailError.value = stringResource(R.string.mail_is_not_in_form)
            false
        } else {
            _emailError.value = ""
            true
        }
    }

    private fun validatePasswordLength(): Boolean {
        return if (!(password1Content.value?.length!! >= passwordLength)) {
            _passwordErrorLength.value =
                stringResource(R.string.passwor_must_have) + passwordLength + stringResource(R.string.number_of_characters)
            false
        } else {
            _passwordErrorLength.value = ""
            true
        }
    }

    private fun validateSamePassword(): Boolean {
        return if (password1Content.value != password2Content.value) {
            _passwordErrorSimilarity.value = stringResource(R.string.password_not_same)
            false
        } else {
            _passwordErrorSimilarity.value = ""
            true
        }
    }


    fun createUserInFirebaseAuth(email: String, password: String): Task<AuthResult> {

        val registrationTask =
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) { // Registrace proběhla úspěch
                        Log.d("Registrace", "Úspěch")
                    } else { // Registrace neproběhla úspěšně
                        Log.d("Registrace", "Neúspěch")
                    }
                }
        return registrationTask
    }

    //toFirestore
    fun createUserInFirestore(name: String, email: String): Task<DocumentReference> {
        val user: MutableMap<String, Any> = HashMap()
        user["name"] = name
        user["email"] = email

        val createUserInDb = db.collection("users") .add(user)
        return createUserInDb
    }
}
