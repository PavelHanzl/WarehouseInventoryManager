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
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.menu_header.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivityViewModel : BaseViewModel() {
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _profilePhoto = MutableLiveData<Bitmap>()
    val profilePhoto: LiveData<Bitmap> get() = _profilePhoto

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
                var byteArray = imageRef.getBytes(5L * 1024 * 1024).await()
                var bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                imageRef.downloadUrl.addOnSuccessListener {
                    Log.d("ImageRef Download URL", "URL: $it")
                }

                _profilePhoto.postValue(bmp)
            } catch (e: StorageException) {
                Log.d("Header", "Obrázek nejde načíst! Udělej s tim něco! + ${e.message}")
            }
        }
    }
}