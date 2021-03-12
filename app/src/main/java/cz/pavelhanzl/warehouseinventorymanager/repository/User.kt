package cz.pavelhanzl.warehouseinventorymanager.repository

import android.provider.ContactsContract

data class User(
    var name: String = "",
    var email: String = "" ,
    var photoURL: String = ""
)