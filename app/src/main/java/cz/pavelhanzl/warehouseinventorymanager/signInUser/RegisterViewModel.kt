package cz.pavelhanzl.warehouseinventorymanager.signInUser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

/**
 * Register view model
 *
 * @constructor Create empty Register view model
 */
class RegisterViewModel : BaseViewModel() {

    private val passwordLength = Constants.MIN_PASSWORD_LENGTH

    val allowRegistration = MutableLiveData<Boolean>(false)

    // proměné, které může měnit View a jsou nabindované přes XML
    val nameContent = MutableLiveData<String>("")
    val emailContent = MutableLiveData<String>("")
    val password1Content = MutableLiveData<String>("")
    val password2Content = MutableLiveData<String>("")

    // pár otevřené (Mutable) a uzavřené (ne Mutable) proměnné,
    // které nemůže měnit view
    private val _nameError = MutableLiveData<String>()
    val nameError: LiveData<String> get() = _nameError

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> get() = _emailError

    private val _passwordErrorLength = MutableLiveData<String>()
    val passwordErrorLength: LiveData<String> get() = _passwordErrorLength

    private val _passwordErrorSimilarity = MutableLiveData<String>()
    val passwordErrorSimilarity: LiveData<String> get() = _passwordErrorSimilarity

    private val _status = MutableLiveData<String>("")
    val status: LiveData<String> get() = _status

    private val _moveToDashboard = MutableLiveData<Boolean>()
    val moveToDashboard: LiveData<Boolean> get() = _moveToDashboard

    ///

    /**
     * Defines what should happen when user click on register button click
     *
     */
    fun onRegisterClick() {

        //check if inputs are valid
        if (validForRegistration()) {

            viewModelScope.launch(Dispatchers.IO) {

                try {
                    createUserInFirebaseAuth(
                        emailContent.value!!,
                        password1Content.value!!
                    ).await()
                } catch (e: Exception) {
                    _status.postValue(e.message)
                    return@launch
                }

                try {
                    createUserInFirestore(
                        nameContent.value!!,
                        emailContent.value!!
                    ).await()
                } catch (e: Exception) {
                    _status.postValue(e.localizedMessage)
                    FirebaseAuth.getInstance().currentUser!!.delete()
                    return@launch
                }

                _moveToDashboard.postValue(true)
            }


        }
    }

    /**
     * Checks if all inputs are valid for registration
     *
     * @return returns true if valid
     */
    fun validForRegistration(): Boolean {

        val nameValidation = validateName()
        val emailValidation = validateEmail()
        val samePasswordValidation = validateSamePassword()
        val lengthPasswordValidation = validatePasswordLength()

        return     nameValidation
                && emailValidation
                && samePasswordValidation
                && lengthPasswordValidation
    }

    /**
     * Validates given name
     *
     * @return returns true if valid
     */
    private fun validateName(): Boolean {
        return if (nameContent.value?.isEmpty()!!) {
            _nameError.value = stringResource(R.string.type_in_name)
            false
        } else {
            _nameError.value = ""
            true
        }
    }

    /**
     * Validates given email format
     *
     * @return returns true if valid
     */
    private fun validateEmail(): Boolean {
        return if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(emailContent.value!!)
                .matches()
        ) {
            _emailError.value = stringResource(R.string.mail_is_not_in_form)
            false
        } else {
            _emailError.value = ""
            true
        }
    }

    /**
     * Validates given password length
     *
     * @return returns true if valid
     */
    private fun validatePasswordLength(): Boolean {
        return if (!(password1Content.value?.length!! >= passwordLength)) {
            _passwordErrorLength.value =
                stringResource(
                    R.string.password_must_have
                ) + passwordLength + stringResource(
                    R.string.number_of_characters
                )
            false
        } else {
            _passwordErrorLength.value = ""
            true
        }
    }

    /**
     * Validates if both given passwords are the same
     *
     * @return returns true if valid
     */
    private fun validateSamePassword(): Boolean {
        return if (password1Content.value != password2Content.value) {
            _passwordErrorSimilarity.value =
                stringResource(R.string.password_not_same)
            false
        } else {
            _passwordErrorSimilarity.value = ""
            true
        }
    }

    /**
     * Creates user in firebase auth
     *
     * @param email email of the user
     * @param password password of the user
     * @return returns task
     */
    fun createUserInFirebaseAuth(
        email: String,
        password: String
    ): Task<AuthResult> {
        return FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
    }

    /**
     * Creates user in firestore database
     *
     * @param name users given name
     * @param email users email
     * @return returns task
     */
    fun createUserInFirestore(name: String, email: String): Task<Void> {

        val user: MutableMap<String, Any> = HashMap()
        user["userID"] = auth.currentUser!!.uid
        user["name"] = name
        user["email"] = email
        user["photoURL"] = ""

        return db.collection(Constants.USERS_STRING)
            .document(FirebaseAuth.getInstance().currentUser!!.uid).set(user)
    }
}
