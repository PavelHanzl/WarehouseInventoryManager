package cz.pavelhanzl.warehouseinventorymanager.service

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import cz.pavelhanzl.warehouseinventorymanager.repository.RepoComunicationLayer

abstract class BaseFragment: Fragment(){
    val db = Firebase.firestore
    val auth = Firebase.auth
    val storage = Firebase.storage.reference
    val repoComunicationLayer = RepoComunicationLayer()
}