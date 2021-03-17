package cz.pavelhanzl.warehouseinventorymanager.repository

import java.sql.Time

data class Warehouse (
    var warehouseID: String = "",
    var name: String = "",
    var note: String = "",
    var owner: String = "",
    var photoURL: String = "",
    var createDate: String = getDateTime()
    )