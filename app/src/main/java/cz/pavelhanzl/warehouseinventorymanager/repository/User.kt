package cz.pavelhanzl.warehouseinventorymanager.repository

import android.provider.ContactsContract

data class User(
    var userID: String = "",
    var name: String = "",
    var email: String = "" ,
    var photoURL: String = ""
)