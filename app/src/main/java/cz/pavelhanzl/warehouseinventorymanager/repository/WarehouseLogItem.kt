package cz.pavelhanzl.warehouseinventorymanager.repository

import com.google.firebase.auth.FirebaseAuth

/**
 * Warehouse log item
 *
 * @property logMessage
 * @property itemName
 * @property itemCount
 * @property date default is current datetime of device
 * @property userName default is email of currently logged in user
 * @constructor Create empty Warehouse log item
 */
data class WarehouseLogItem(
        var logMessage: String = "",
        var itemName: String = "",
        var itemCount: String = "",
        var date: String = getDateTime(),
        var userName: String = FirebaseAuth.getInstance().currentUser!!.email!!
)

