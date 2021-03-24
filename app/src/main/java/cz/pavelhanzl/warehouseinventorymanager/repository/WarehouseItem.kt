package cz.pavelhanzl.warehouseinventorymanager.repository

import java.sql.Time

data class WarehouseItem (
    var warehouseItemID: String = "",
    var name: String = "",
    var note: String = "",
    var code: String = "",
    var photoURL: String = "",
    var createDate: String = getDateTime(),
    var price: String = "",
    var count: String = ""
    )