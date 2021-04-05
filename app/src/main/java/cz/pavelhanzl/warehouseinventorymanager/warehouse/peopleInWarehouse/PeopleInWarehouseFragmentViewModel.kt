package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.repository.*
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.acl.Owner

class PeopleInWarehouseFragmentViewModel : BaseViewModel() {
    lateinit var warehouse: Warehouse
    lateinit var owner: User

    var userEmailContent = MutableLiveData<String>("")
    var _userEmailError = MutableLiveData<String>("")
    val userEmailError: LiveData<String> get() = _userEmailError

    var _ownerName = MutableLiveData<String>("")
    val ownerName: LiveData<String> get() = _ownerName

    var _ownerPhotoUrl = MutableLiveData<String>("")
    val ownerPhotoUrl: LiveData<String> get() = _ownerPhotoUrl

    fun setData(warehouse: Warehouse) {
        this.warehouse = warehouse

        GlobalScope.launch(Dispatchers.IO) {
            val ownerRef = db.collection(Constants.USERS_STRING).document(warehouse.owner)
            ownerRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    owner = it.result!!.toObject(User::class.java)!!
                    _ownerName.postValue(owner.name)
                    _ownerPhotoUrl.postValue(owner.photoURL)

                }
            }
        }

    }


    suspend fun inviteUser(){
        var invitedUser = User()

        //todo
        Log.d("btn pressed","1")
        GlobalScope.launch(Dispatchers.IO) {
        val invitedUserRef = db.collection(Constants.USERS_STRING).whereEqualTo("email", userEmailContent.value.toString()).limit(1)
        invitedUserRef.get().addOnCompleteListener {
            if(it.isSuccessful){
                invitedUser = it.result!!.documents[0].toObject(User::class.java)!!
               Log.d("btn pressed","4" + " " + invitedUser.name)
            }
        }}.join()

        Log.d("btn pressed","2")
        GlobalScope.launch(Dispatchers.IO) {
        val invitationDocRef = db.collection(Constants.INVITATIONS_STRING).document()

        //vytvoří instanci pozvánky
        val invitation = Invitation()

        invitation.invitationId = invitationDocRef.id
        invitation.warehouseId = warehouse.warehouseID
        invitation.from = warehouse.owner
        invitation.to = invitedUser.userID
            Log.d("btn pressed","3" + " " + invitation.invitationId + " | " + invitation.warehouseId+ " | " +invitation.from+ " | " +invitation.to )

        //zapíše do předpřipraveného dokumentu
        invitationDocRef.set(invitation).await()
        }

    }


}