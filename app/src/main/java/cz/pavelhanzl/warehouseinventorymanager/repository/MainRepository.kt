package cz.pavelhanzl.warehouseinventorymanager.repository

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R

class MainRepository {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val storage = Firebase.storage.reference

    fun getUser(): FirebaseUser?{
        return  auth.currentUser
     }

    fun getOwnWarehouses(): Task<QuerySnapshot> {
        return db.collection("warehouses").get()
    }
}