package cz.pavelhanzl.warehouseinventorymanager.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

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

    fun registrateBadgeListeners() {
        Log.d("badgers", "registruju badgery")

        invitationRecievedListener = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("to", auth.currentUser!!.uid).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w("Data pro invi badge", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.documents.size != 0) {
                _invitationRecievedBadgeVisible.value = true
                _invitationRecievedNumber.value = snapshot.documents.size.toString()

            } else {
                Log.d("Data pro invi badge", "Current data: null")
                _invitationRecievedBadgeVisible.value = false
                _invitationRecievedNumber.value = "0"
            }

        }

        invitationSendListener = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("from", auth.currentUser!!.uid).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w("Data pro invi badge", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.documents.size != 0) {
                _invitationSendBadgeVisible.value = true
                _invitationSendNumber.value = snapshot.documents.size.toString()

            } else {
                Log.d("Data pro invi badge", "Current data: null")
                _invitationSendBadgeVisible.value = false
                _invitationSendNumber.value = "0"
            }

        }


    }

    fun unregistrateBadgeListeners() {
        Log.d("badgers", "odpojuji badgery")
        invitationRecievedListener.remove()
        invitationSendListener.remove()
    }


}