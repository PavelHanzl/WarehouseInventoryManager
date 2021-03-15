package cz.pavelhanzl.warehouseinventorymanager.repository

data class Warehouse (
    var warehouseID: String = "",
    var name: String = "",
    var note: String = "",
    var owner: String = "",
    var photoURL: String = ""
    )