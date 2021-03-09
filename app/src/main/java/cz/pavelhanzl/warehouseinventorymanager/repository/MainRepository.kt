package cz.pavelhanzl.warehouseinventorymanager.repository

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R

object MainRepository {


     fun createAccount(email: String, password: String) {
         FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(
            OnCompleteListener<AuthResult> { task ->
                if(task.isSuccessful){ // Registrace proběhla úspěšně
                    val firebaseUser = task.result!!.user!!
                    Log.d("Registrace","Úspěch")
                } else { // Registrace neproběhla úspěšně
                    Log.d("Registrace","Neúspěch")
                }

            }
        )
    }

    @SuppressLint("StaticFieldLeak")
    val db = FirebaseFirestore.getInstance()



    fun writeUser() {
        // Create a new user with a first and last name
        // Create a new user with a first and last name
        val user: MutableMap<String, Any> = HashMap()
        user["first"] = "Ada"
        user["last"] = "Lovelace"
        user["born"] = 1815


// Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "rte",
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
            }
            .addOnFailureListener { e -> Log.w("rte", "Error adding document", e) }
    }
}