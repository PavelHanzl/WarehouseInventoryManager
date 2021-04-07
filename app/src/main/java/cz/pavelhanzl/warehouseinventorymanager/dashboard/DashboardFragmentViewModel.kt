package cz.pavelhanzl.warehouseinventorymanager.dashboard

import android.media.MediaSession2Service
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse.PeopleInWarehouseAdapter

class DashboardFragmentViewModel:BaseViewModel() {

    var _notificationNumber = MutableLiveData<String>("")
    val notificationNumber: LiveData<String> get() = _notificationNumber

    var _invitationNumber = MutableLiveData<String>("")
    val invitationNumber: LiveData<String> get() = _invitationNumber

    private val _notificationBadgeVisible = MutableLiveData<Boolean>(false)
    val notificationBadgeVisible: LiveData<Boolean> get() = _notificationBadgeVisible

    private val _invitationBadgeVisible = MutableLiveData<Boolean>(false)
    val invitationBadgeVisible: LiveData<Boolean> get() = _invitationBadgeVisible

    lateinit var invitationListener: ListenerRegistration
    lateinit var notificationListener: ListenerRegistration

    fun registrateBadgeListeners(){
        Log.d("badgers", "registruju badgery")

        invitationListener = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("to", auth.currentUser!!.uid).addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w("Data pro invi badge", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.documents.size != 0) {
                _invitationBadgeVisible.value= true
                _invitationNumber.value = snapshot.documents.size.toString()

            } else {
                Log.d("Data pro invi badge", "Current data: null")
                _invitationBadgeVisible.value= false
                _invitationNumber.value = "0"
            }

        }




    }

    fun unregistrateBadgeListeners(){
        Log.d("badgers", "odpojuji badgery")
        invitationListener.remove()

        //TODO napojit notifikace
//        notificationListener.remove()
    }



}