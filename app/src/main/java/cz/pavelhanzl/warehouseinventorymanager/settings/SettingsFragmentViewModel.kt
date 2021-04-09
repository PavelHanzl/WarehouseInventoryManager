package cz.pavelhanzl.warehouseinventorymanager.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsFragmentViewModel : BaseViewModel() {

    var userCurrentNameContent = MutableLiveData<String>("")
    var _userCurrentNameError = MutableLiveData<String>("")
    val userCurrentNameError: LiveData<String> get() = _userCurrentNameError

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
        //data class CreateEdit(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun changePassword() {
        GlobalScope.launch(Main) {
            if (validatePasswords()) {
                Log.d("CAJ", "Jsme ready menit hesla")
                auth.currentUser.updatePassword(userNewPassword1Content.value).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        GlobalScope.launch(Main) { eventChannel.send(Event.NavigateBack)}
                        Log.d("CAJ", "Heslo zmeneno")
                    } else {
                        Log.d("CAJ", "NEco se posralo")
                    }
                }
            } else {
                //eventChannel.send(Event.HideLoading)
                Log.d("CAJ", "Nejsme ready menit hesla")
            }

        }
    }

    suspend fun validatePasswords(): Boolean {
        if (userCurrentPasswordContent.value == "") {
            _userCurrentPasswordError.postValue("Heslo nemůže být prázdné")
            return false
        } else {
            _userCurrentPasswordError.postValue("")
        }

        val currentPasswordEmailResult = validateCurrentEmail()
        val currentPasswordValidResult = validateCurrentPassword()
        val samePasswordValidResult = validateNewPasswordSame()
        val newPasswordLength = validatePasswordLenght()

        if (currentPasswordValidResult) {
            _userCurrentPasswordError.postValue("")
        } else {
            _userCurrentPasswordError.postValue("Zadané heslo není správné")
        }

        return currentPasswordValidResult && samePasswordValidResult && newPasswordLength && currentPasswordEmailResult
    }

    private fun validateCurrentEmail(): Boolean {

        return if (auth.currentUser?.email == userCurrentEmailContent.value) {
            _userCurrentEmailError.postValue("")
            true
        } else {
            _userCurrentEmailError.postValue("Toto není e-mail přidružený k tomuto účtu")
            false
        }
    }

    suspend private fun validateCurrentPassword(): Boolean {
        val result = CompletableDeferred<Boolean>()
        val credential = EmailAuthProvider.getCredential(auth.currentUser.email, userCurrentPasswordContent.value)
        auth.currentUser.reauthenticate(credential).addOnCompleteListener { task ->
            result.complete(task.isSuccessful)
        }
        return result.await()
    }

    private fun validateNewPasswordSame(): Boolean {
        return if (userNewPassword1Content.value == userNewPassword2Content.value) {
            _userNewPassword2Error.value = ""
            true
        } else {
            _userNewPassword2Error.value = "Hesla se neshodují"
            false
        }
    }

    private fun validatePasswordLenght(): Boolean {

        return if (userNewPassword1Content.value!!.length < Constants.MIN_PASSWORD_LENGTH) {
            _userNewPassword1Error.value = stringResource(R.string.password_must_have) + Constants.MIN_PASSWORD_LENGTH + stringResource(R.string.number_of_characters)
            false
        } else {
            _userNewPassword1Error.value = ""
            true
        }
    }


}