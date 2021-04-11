package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Time

/**
 * Warehouse item
 * Warehouse item data object class.
 *
 * @property warehouseItemID
 * @property warehouseID
 * @property name
 * @property name_lowercase needed because of sorting in firebase is case sensitive
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
    var name_lowercase: String = "", //potřeba jelikož firestore řadí v módu case sensitive, tudíž pak řazení pro normálního smrtelníka nedává smysl, query se tedy řadí podle tohoto pole
    var note: String = "",
    var code: String = "",
    var photoURL: String = "",
    var createDate: String = getDateTime(),
    var price: Double = 0.0,
    var count: Double = 0.0
    ): Parcelable