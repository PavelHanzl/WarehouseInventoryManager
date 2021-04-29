package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Warehouse item
 * Warehouse item data object class.
 *
 * @property warehouseItemID
 * @property warehouseID
 * @property name
 * @property name_lowercase needed because of sorting in db is case sensitive
 * @property note
 * @property code
 * @property photoURL
 * @property createDate default is current datetime of device
 * @property price
 * @property count
 * @constructor Create empty Warehouse item
 */
@Parcelize
data class WarehouseItem (
    var warehouseItemID: String = "",
    var warehouseID: String = "",
    var name: String = "",
    var name_lowercase: String = "",
    var note: String = "",
    var code: String = "",
    var photoURL: String = "",
    var createDate: String = getDateTime(),
    var price: Double = 0.0,
    var count: Double = 0.0
    ): Parcelable