package cz.pavelhanzl.warehouseinventorymanager.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

/**
 * Dashboard fragment view model
 *
 * @constructor Create empty Dashboard fragment view model
 */
class DashboardFragmentViewModel : BaseViewModel() {

    var _invitationSendNumber = MutableLiveData<String>("")
    val invitationSendNumber: LiveData<String> get() = _invitationSendNumber

    var _invitationRecievedNumber = MutableLiveData<String>("")
    val invitationRecievedNumber: LiveData<String> get() = _invitationRecievedNumber

    private val _invitationSendBadgeVisible = MutableLiveData<Boolean>(false)
    val invitationSendBadgeVisible: LiveData<Boolean> get() = _invitationSendBadgeVisible

    private val _invitationRecievedBadgeVisible = MutableLiveData<Boolean>(false)
    val invitationRecievedBadgeVisible: LiveData<Boolean> get() = _invitationRecievedBadgeVisible

    lateinit var invitationRecievedListener: ListenerRegistration
    lateinit var invitationSendListener: ListenerRegistration

    /**
     * Registrate badge listeners
     * Register red badgets shown in upper left corners of buttons on dashboard screen, which show a number of pending invitations.
     */
    fun registrateBadgeListeners() {

        //Recieved invitations
        invitationRecievedListener = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("to", auth.currentUser!!.uid).addSnapshotListener { snapshot, e ->

            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.documents.size != 0) {
                _invitationRecievedBadgeVisible.value = true
                _invitationRecievedNumber.value = snapshot.documents.size.toString()

            } else {
                _invitationRecievedBadgeVisible.value = false
                _invitationRecievedNumber.value = "0"
            }

        }

        //Send invitations
        invitationSendListener = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("from", auth.currentUser!!.uid).addSnapshotListener { snapshot, e ->

            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.documents.size != 0) {
                _invitationSendBadgeVisible.value = true
                _invitationSendNumber.value = snapshot.documents.size.toString()

            } else {
                _invitationSendBadgeVisible.value = false
                _invitationSendNumber.value = "0"
            }

        }


    }

    /**
     * Unregistrate badge listeners
     * Removes listeners when called.
     */
    fun unregistrateBadgeListeners() {
        invitationRecievedListener.remove()
        invitationSendListener.remove()
    }


}