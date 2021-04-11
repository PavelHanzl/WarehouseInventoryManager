package cz.pavelhanzl.warehouseinventorymanager.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Time

/**
 * Invitation
 * Invitation data object class.
 *
 * @property invitationId
 * @property warehouseId
 * @property from
 * @property to
 * @property date default is current datetime of device
 * @constructor Create empty Invitation
 */
//parcelize z toho důodu, abychom mohli tuto tídu posílat v safeargs v navigation component
@Parcelize
data class Invitation (
    var invitationId: String = "",
    var warehouseId: String = "",
    var from: String = "",
    var to: String = "",
    var date: String = getDateTime()
    ): Parcelable