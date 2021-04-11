package cz.pavelhanzl.warehouseinventorymanager.settings

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Settings fragment view model
 *
 * @constructor Create empty Settings fragment view model
 */
class SettingsFragmentViewModel : BaseViewModel() {

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    var userProfilePhoto = MutableLiveData<ByteArray>()
    var _profilePhotoUrl = MutableLiveData<String>("")
    val profilePhotoUrl: LiveData<String> get() = _profilePhotoUrl

    var userNewNameContent = MutableLiveData<String>("")
    var _userNewNameError = MutableLiveData<String>("")
    val userNewNameError: LiveData<String> get() = _userNewNameError

    var userCurrentEmailContent = MutableLiveData<String>("")
    var _userCurrentEmailError = MutableLiveData<String>("")
    val userCurrentEmailError: LiveData<String> get() = _userCurrentEmailError

    var userNewEmailContent = MutableLiveData<String>("")
    var _userNewEmailError = MutableLiveData<String>("")
    val userNewEmailError: LiveData<String> get() = _userNewEmailError

    var userCurrentPasswordContent = MutableLiveData<String>("")
    var _userCurrentPasswordError = MutableLiveData<String>("")
    val userCurrentPasswordError: LiveData<String> get() = _userCurrentPasswordError

    var userNewPassword1Content = MutableLiveData<String>("")
    var _userNewPassword1Error = MutableLiveData<String>("")
    val userNewPassword1Error: LiveData<String> get() = _userNewPassword1Error

    var userNewPassword2Content = MutableLiveData<String>("")
    var _userNewPassword2Error = MutableLiveData<String>("")
    val userNewPassword2Error: LiveData<String> get() = _userNewPassword2Error

    sealed class Event {
        object NavigateBack : Event()
        object NoPhotoSelected : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun wipeData() {

        _loading.value = false
        _profilePhotoUrl.value = ""
        userProfilePhoto.value = byteArrayOf()


        userNewNameContent.value = ""
        _userNewNameError.value = ""

        userCurrentEmailContent.value = ""
        _userCurrentEmailError.value = ""

        userNewEmailContent.value = ""
        _userNewEmailError.value = ""

        userCurrentPasswordContent.value = ""
        _userCurrentPasswordError.value = ""

        userNewPassword1Content.value = ""
        _userNewPassword1Error.value = ""

        userNewPassword2Content.value = ""
        _userNewPassword2Error.value = ""

    }

    /////////////////////////////////////////////////////začátek změna hesla//////////////////////////////////////////////////
    /**
     * Changes users password to his or her account
     */
    fun changePassword() {
        _loading.value = true
        GlobalScope.launch(Main) {
            if (validateForChangePasswords()) {
                auth.currentUser.updatePassword(userNewPassword1Content.value).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        GlobalScope.launch(Main) { eventChannel.send(Event.NavigateBack) }
                        _loading.value = false
                    } else {
                        _loading.value = false
                    }
                }
            } else {
                _loading.value = false
            }

        }
    }

    /**
     * Validate all inputs before changing password
     * @return returns true if valid
     */
    suspend fun validateForChangePasswords(): Boolean {
        if (userCurrentPasswordContent.value == "") {
            _userCurrentPasswordError.postValue(stringResource(R.string.passwordCanNotBeEmpty))
            return false
        } else {
            _userCurrentPasswordError.postValue("")
        }

        val currentEmailValidResult = validateCurrentEmail()
        val currentPasswordValidResult = validateCurrentPassword()
        val samePasswordValidResult = validateNewPasswordSame()
        val newPasswordLength = validatePasswordLenght()

        if (currentPasswordValidResult) {
            _userCurrentPasswordError.postValue("")
        } else {
            _userCurrentPasswordError.postValue(stringResource(R.string.EnteredPasswordIsNotRight))
        }

        return currentPasswordValidResult && samePasswordValidResult && newPasswordLength && currentEmailValidResult
    }

    /**
     * Validate current email before changing it
     *
     * @return returns true if valid
     */
    private fun validateCurrentEmail(): Boolean {
        var result: Boolean
        if (auth.currentUser?.email == userCurrentEmailContent.value) {
            _userCurrentEmailError.postValue("")
            result = true
        } else {
            _userCurrentEmailError.postValue(stringResource(R.string.ThisIsNotEmailAccountConnectedToThisUserAccount))
            result = false
        }

        if (userCurrentEmailContent.value == "") {
            _userCurrentEmailError.postValue(stringResource(R.string.thisFieldCanNotBeEmpty))
            result = false
        }
        return result
    }

    /**
     * Validate current password before changing it
     *
     * @return returns true if valid
     */
    suspend private fun validateCurrentPassword(): Boolean {
        val result = CompletableDeferred<Boolean>()
        val credential = EmailAuthProvider.getCredential(auth.currentUser?.email!!, userCurrentPasswordContent.value!!)

        auth.currentUser?.reauthenticate(credential)!!.addOnCompleteListener { task ->
            result.complete(task.isSuccessful)
        }
        return result.await()
    }

    private fun validateNewPasswordSame(): Boolean {
        return if (userNewPassword1Content.value == userNewPassword2Content.value) {
            _userNewPassword2Error.value = ""
            true
        } else {
            _userNewPassword2Error.value = stringResource(R.string.passwordsDoNotMatch)
            false
        }
    }

    /**
     * Validate password lenght
     *
     * @return returns true if valid
     */
    private fun validatePasswordLenght(): Boolean {

        return if (userNewPassword1Content.value!!.length < Constants.MIN_PASSWORD_LENGTH) {
            _userNewPassword1Error.value = stringResource(R.string.password_must_have) + Constants.MIN_PASSWORD_LENGTH + stringResource(R.string.number_of_characters)
            false
        } else {
            _userNewPassword1Error.value = ""
            true
        }
    }

    /////////////////////////////////////////////////////konec změna hesla//////////////////////////////////////////////////

    /////////////////////////////////////////////////////začátek změna emailu//////////////////////////////////////////////////

    /**
     * Changes user email to his or her account
     */
    fun changeEmail() {
        _loading.value = true
        GlobalScope.launch(Main) {
            if (validateForChangeEmail()) {
                auth.currentUser.updateEmail(userNewEmailContent.value).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        GlobalScope.launch(Main) { eventChannel.send(Event.NavigateBack) }
                        _loading.value = false
                    } else {
                        _loading.value = false
                    }
                }
            } else {
                _loading.value = false
            }
        }
    }

    /**
     * Validate all inputs before changing email
     *
     * @return returns true if valid
     */
    suspend fun validateForChangeEmail(): Boolean {

        val currentEmailValidResult = validateCurrentEmail()
        val newEmailValidResult = validateNewEmail()

        if (userCurrentPasswordContent.value == "") {
            _userCurrentPasswordError.postValue(stringResource(R.string.passwordCanNotBeEmpty))
            return false
        } else {
            _userCurrentPasswordError.postValue("")
        }

        val currentPasswordValidResult = validateCurrentPassword()

        if (currentPasswordValidResult) {
            _userCurrentPasswordError.postValue("")
        } else {
            _userCurrentPasswordError.postValue(stringResource(R.string.EnteredPasswordIsNotRight))
        }

        return currentEmailValidResult && currentPasswordValidResult && newEmailValidResult
    }

    /**
     * Validates input with new email
     *
     * @return returns true if valid
     */
    private fun validateNewEmail(): Boolean {
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userNewEmailContent.value!!).matches() || userNewEmailContent.value == "") {
            _userNewEmailError.value = stringResource(R.string.mail_is_not_in_form)
            false
        } else {
            _userNewEmailError.value = ""
            true
        }
    }

    /////////////////////////////////////////////////////konec změna hesla//////////////////////////////////////////////////

    /////////////////////////////////////////////////////začátek změna jména//////////////////////////////////////////////////

    /**
     * Changes users name which is displayed in application
     */
    fun changeName() {
        if (!userNewNameContent.value.isNullOrEmpty()) {
            _userNewNameError.value = ""
            GlobalScope.launch(IO) {
                _loading.postValue(true)
                db.collection(Constants.USERS_STRING).document(auth.currentUser.uid).update(Constants.NAME_STRING, userNewNameContent.value).await()
                _loading.postValue(false)
                eventChannel.send(Event.NavigateBack)
            }
        } else {
            _userNewNameError.value = stringResource(R.string.thisFieldCanNotBeEmpty)
            _loading.postValue(false)
        }


    }
    /////////////////////////////////////////////////////konec změna jména/////////////////////////////////////////////////

    /////////////////////////////////////////////////////začátek změna fotky//////////////////////////////////////////////////

    /**
     * Gets current users photo which is displayed in application
     */
    fun getUsersPhoto() {
        GlobalScope.launch(IO) {
            try {
                _loading.postValue(true)
                _profilePhotoUrl.postValue(storage.child("images/users/" + auth.currentUser?.uid + "/profile.jpg").downloadUrl.await().toString())
                _loading.postValue(false)
            } catch (e: StorageException) {
                _loading.postValue(false)
            }

        }
    }

    /**
     * Changes users photo which is displayed in application
     */
    fun changePhoto() {
        if (userProfilePhoto.value!!.isNotEmpty()) {
            GlobalScope.launch(IO) {
                _loading.postValue(true)
                try {
                    //ukladam do storage
                    storage.child("/images/users/${auth.currentUser?.uid}/profile.jpg").putBytes(userProfilePhoto.value!!).await()

                    //ziskavam url prave ulozeneho obrazku
                    val newPhotoUrl = storage.child("/images/users/${auth.currentUser?.uid}/profile.jpg").downloadUrl.await()

                    //zapisuji url obrazku k objektu usera v DB
                    db.collection(Constants.USERS_STRING).document(auth.currentUser!!.uid).update("photoURL", newPhotoUrl.toString())
                    _loading.postValue(false)

                    eventChannel.send(Event.NavigateBack) //zavirame fragment
                } catch (e: Exception) {
                    _loading.postValue(false)
                }
            }
        } else {
            GlobalScope.launch(Main) { eventChannel.send(Event.NoPhotoSelected) }
        }

    }

    /////////////////////////////////////////////////////konec změna fotky/////////////////////////////////////////////////

}