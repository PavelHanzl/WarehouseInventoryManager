package cz.pavelhanzl.warehouseinventorymanager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import cz.pavelhanzl.warehouseinventorymanager.repository.User
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.android.synthetic.main.menu_header.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivityViewModel : BaseViewModel() {
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _profilePhotoUrl = MutableLiveData<String>()
    val profilePhotoUrl: LiveData<String> get() = _profilePhotoUrl


    init {
        getUserName()
        getUserImage()
    }

    fun getUserName() {
        CoroutineScope(Dispatchers.IO).launch {
            val userDocument = db.collection("users").document(auth.currentUser!!.uid)
            userDocument.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("Header", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("Header", "Current data: ${snapshot.data}")
                    _name.value = snapshot.toObject<User>()!!.name
                    _profilePhotoUrl.postValue(snapshot.toObject<User>()!!.photoURL)// trigne observer na fotku v drawer menu

                } else {
                    Log.d("Header", "Current data: null")
                }
            }
        }
    }

    fun getUserImage() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var imageRef = storage.child("images/users/" + auth.currentUser!!.uid + "/profile.jpg")

                _profilePhotoUrl.postValue( imageRef.downloadUrl.await().toString())

                imageRef.downloadUrl.addOnSuccessListener {
                    Log.d("ImageRef Download URL", "URL: $it")
                }

            } catch (e: StorageException) {
                Log.d("Header", "Drawer image error + ${e.message}")
            }
        }


    }

}