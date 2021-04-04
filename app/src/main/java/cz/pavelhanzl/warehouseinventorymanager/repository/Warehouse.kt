package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Time

//parcelize z toho důodu, abychom mohli tuto tídu posílat v safeargs v navigation component
@Parcelize
data class Warehouse (
    var warehouseID: String = "",
    var name: String = "",
    var name_lowercase: String = "", //potřeba jelikož firestore řadí v módu case sensitive, tudíž pak řazení pro normálního smrtelníka nedává smysl, query se tedy řadí podle tohoto pole
    var note: String = "",
    var owner: String = "",
    var photoURL: String = "",
    var users: List<String> = emptyList(),
    var createDate: String = getDateTime()
    ): Parcelable