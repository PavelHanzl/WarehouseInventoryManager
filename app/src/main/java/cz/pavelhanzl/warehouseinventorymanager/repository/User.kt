package cz.pavelhanzl.warehouseinventorymanager.repository

import android.provider.ContactsContract

/**
 * User
 * User data object class.
 *
 * @property userID
 * @property name
 * @property email
 * @property photoURL
 * @constructor Create empty User
 */
data class User(
    var userID: String = "",
    var name: String = "",
    var email: String = "" ,
    var photoURL: String = ""
)