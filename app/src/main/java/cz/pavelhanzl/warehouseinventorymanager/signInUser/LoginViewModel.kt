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


 fun onLoginClick(){
  Log.d("LoginActivity", "Klikáš na login vole!")

  if( validForLogin()){
   login(emailContent.value!!,passwordContent.value!!)
   Log.d("LoginActivity", "je to valid!")
  }
 }

 fun validForLogin(): Boolean {

  val emailValidation = validateEmail()
  val lengthPasswordValidation = validatePasswordLength()

  return emailValidation && lengthPasswordValidation
 }

 private fun validatePasswordLength(): Boolean {
  return if (!(passwordContent.value?.length!! >= passwordLength)) {
   _passwordError.value =
    stringResource(R.string.passwor_must_have) + passwordLength + stringResource(R.string.number_of_characters)
   false
  } else {
   _passwordError.value = ""
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

 //provede login na základě emailu a hesla
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

fun saveUserProfilePhotoFromGoogleAuth(): UploadTask {
  var userImageURL = auth.currentUser!!.photoUrl.toString()
  var photoRef = storage.child("images/" + auth.currentUser!!.uid + "/profile.jpg")
  val picture = Picasso.get().load(userImageURL).get()
  val baos = ByteArrayOutputStream()
  picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
  val data = baos.toByteArray()
  return photoRef.putBytes(data)
 }

 fun createUserInFirestore(name: String, email: String): Task<Void> {

  val user: MutableMap<String, Any> = HashMap()
  user["name"] = name
  user["email"] = email

  return db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
   .set(user)

 }

 fun firebaseAuthWithGoogleLogic(credential: AuthCredential){
  GlobalScope.launch(Dispatchers.IO) {
   try {
    auth.signInWithCredential(credential).await()
    Log.d("Firestore", "Login with google")
   } catch (e: FirebaseAuthException) {
    Log.d("Firestore", "Google down: " + "${e.message}")
    return@launch
   }

   try {
    val userDocumentRef =
     db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
      .get().await()
    if (userDocumentRef.exists()) {
     _moveToDashboard.postValue(true)
     return@launch
    }
   } catch (e: Exception) {
    Log.d("Firestore", "Reference dokumentu usera nenalezena!" + "${e.message}")
   }

   try {
    createUserInFirestore(
     auth.currentUser!!.displayName.toString(),
     auth.currentUser!!.email.toString()
    ).await()
   } catch (e: Exception) {
     auth.currentUser!!.delete()
     return@launch
   }

   try {
     saveUserProfilePhotoFromGoogleAuth()
   } catch (e: Exception){
    Log.d("Storage", "Nahrávání obrázku neprošlo!" + "${e.message}")
   }

   _moveToDashboard.postValue(true)

  }
 }

}

