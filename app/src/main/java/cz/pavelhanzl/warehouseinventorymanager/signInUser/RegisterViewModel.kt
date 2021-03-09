package cz.pavelhanzl.warehouseinventorymanager.signInUser

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import cz.pavelhanzl.warehouseinventorymanager.repository.MainRepository

class RegisterViewModel : ViewModel() {

    private val passwordLength = 6

    val authRepository = MainRepository

    val nameContent = MutableLiveData<String>("")
    val emailContent = MutableLiveData<String>("")
    val password1Content = MutableLiveData<String>("")
    val password2Content = MutableLiveData<String>("")

    private val _nameError = MutableLiveData<String>()
    val nameError: LiveData<String> get() = _nameError

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> get() = _emailError

    private val _passwordErrorLength = MutableLiveData<String>()
    val passwordErrorLength: LiveData<String> get() = _passwordErrorLength

    private val _passwordErrorSimilarity = MutableLiveData<String>()
    val passwordErrorSimilarity: LiveData<String> get() = _passwordErrorSimilarity

    ////
    var registerResult = MutableLiveData<String>("")

    val dalsi = MutableLiveData<Boolean>(false)
    ///


    fun onRegisterClick() {
        Log.d("Ahojkyssss", "Klikáš vole")
        if (validForRegistration()) {
            MainRepository.createAccount(emailContent.value!!, password1Content.value!!)
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
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailContent.value).matches()) {
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
}