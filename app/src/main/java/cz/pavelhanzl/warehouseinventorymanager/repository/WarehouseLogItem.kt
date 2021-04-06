package cz.pavelhanzl.warehouseinventorymanager.repository

import com.google.firebase.auth.FirebaseAuth

data class WarehouseLogItem(
        var logMessage: String = "",
        var itemName: String = "",
        var itemCount: String = "",
        var date: String = getDateTime(),
        var userName: String = FirebaseAuth.getInstance().currentUser!!.email!!
)

