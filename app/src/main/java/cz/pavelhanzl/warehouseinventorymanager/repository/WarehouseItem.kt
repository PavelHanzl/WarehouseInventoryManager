package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Time
@Parcelize
data class WarehouseItem (
    var warehouseItemID: String = "",
    var warehouseID: String = "",
    var name: String = "",
    var note: String = "",
    var code: String = "",
    var photoURL: String = "",
    var createDate: String = getDateTime(),
    var price: Double = 0.0,
    var count: Double = 0.0
    ): Parcelable