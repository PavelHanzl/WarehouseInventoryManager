package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Time

//parcelize z toho důodu, abychom mohli tuto tídu posílat v safeargs v navigation component
@Parcelize
data class Warehouse (
    var warehouseID: String = "",
    var name: String = "",
    var note: String = "",
    var owner: String = "",
    var photoURL: String = "",
    var users: List<String> = emptyList(),
    var createDate: String = getDateTime()
    ): Parcelable