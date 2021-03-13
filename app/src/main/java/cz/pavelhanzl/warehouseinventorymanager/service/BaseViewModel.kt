package cz.pavelhanzl.warehouseinventorymanager.service

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

abstract class BaseViewModel: ViewModel(){
    val db = Firebase.firestore
    val auth = Firebase.auth
    val storage = Firebase.storage.reference
}