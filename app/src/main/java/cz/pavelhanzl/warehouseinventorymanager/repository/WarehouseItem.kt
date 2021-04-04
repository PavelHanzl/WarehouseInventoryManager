package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Time
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