package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Time

//parcelize z toho důodu, abychom mohli tuto tídu posílat v safeargs v navigation component
@Parcelize
data class Invitation (
    var invitationId: String = "",
    var warehouseId: String = "",
    var from: String = "",
    var to: String = "", //potřeba jelikož firestore řadí v módu case sensitive, tudíž pak řazení pro normálního smrtelníka nedává smysl, query se tedy řadí podle tohoto pole
    var date: String = getDateTime()
    ): Parcelable