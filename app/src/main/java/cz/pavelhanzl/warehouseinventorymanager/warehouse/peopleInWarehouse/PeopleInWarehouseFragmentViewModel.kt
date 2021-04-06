package cz.pavelhanzl.warehouseinventorymanager.warehouse.peopleInWarehouse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.*
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.security.acl.Owner

class PeopleInWarehouseFragmentViewModel : BaseViewModel() {
    lateinit var warehouse: Warehouse
    lateinit var owner: User
    var valid = true

    var userEmailContent = MutableLiveData<String>("")
    var _userEmailError = MutableLiveData<String>("")
    val userEmailError: LiveData<String> get() = _userEmailError

    var _ownerName = MutableLiveData<String>("")
    val ownerName: LiveData<String> get() = _ownerName

    var _ownerPhotoUrl = MutableLiveData<String>("")
    val ownerPhotoUrl: LiveData<String> get() = _ownerPhotoUrl

    sealed class Event {
        object NavigateBack : Event()
        object PlaySuccessAnimation: Event()

        //data class CreateEdit(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()



    fun setData(warehouse: Warehouse) {
        this.warehouse = warehouse

        GlobalScope.launch(Dispatchers.IO) {
            val ownerRef = db.collection(Constants.USERS_STRING).document(warehouse.owner)
            val ownerDoc = ownerRef.get().await()

            owner = ownerDoc.toObject(User::class.java)!!
            _ownerName.postValue(owner.name)
            _ownerPhotoUrl.postValue(owner.photoURL)

        }

    }

    fun inviteUser() {

        if (!isValid()) return

        GlobalScope.launch(Dispatchers.IO) {
            try {

                //získá objekt usera, ke kterému byl zadán email
                val invitedUserRef = db.collection(Constants.USERS_STRING).whereEqualTo("email", userEmailContent.value.toString()).limit(1)
                val invitedUserQuerySnapshot = invitedUserRef.get().await()

                //zkontroluj jestli nevrátilo prázndou query, coz by znamenalo, ze nikdo s takovým emailem v aplikaci neexistuje
                if(!checkIfUserExistsInDb(invitedUserQuerySnapshot)) return@launch

                val invitedUser = invitedUserQuerySnapshot.documents[0].toObject(User::class.java)

                //zkontroluj jestli pozvaný user už není členem tohoto skladu
                if (checkIfUserIsAreadyInWarehouse(invitedUser!!)) return@launch

                //vytvoří dokument povánky v db
                val invitationDocRef = db.collection(Constants.INVITATIONS_STRING).document()

                //vytvoří instanci pozvánky
                val invitation = Invitation()

                invitation.invitationId = invitationDocRef.id
                invitation.warehouseId = warehouse.warehouseID
                invitation.from = warehouse.owner
                invitation.to = invitedUser.userID
                Log.d("btn pressed", "3" + " " + invitation.invitationId + " | " + invitation.warehouseId + " | " + invitation.from + " | " + invitation.to)

                //zapíše do předpřipraveného dokumentu
                invitationDocRef.set(invitation).await()
                eventChannel.send(Event.PlaySuccessAnimation)
                eventChannel.send(Event.NavigateBack)
            } catch (e: Exception) {
                Log.d("user2", "Exeption:" + e.message)
            }

        }

    }

    private fun isValid(): Boolean {
        valid = true
        _userEmailError.value = ""


        if (userEmailContent.value!!.isEmpty() || userEmailContent.value == "") {
            _userEmailError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        if (auth.currentUser!!.email!! == userEmailContent.value) {
            _userEmailError.value = "Nemůžete pozvat sami sebe! Zadejte jiný e-mail."
            valid = false
        }

        return valid
    }

    fun wipeData() {
        userEmailContent.value = ""
        _userEmailError.value = ""
    }

    private fun checkIfUserIsAreadyInWarehouse(invitedUser: User) : Boolean {
        return warehouse.users.any {
            if(it==invitedUser.userID){
                _userEmailError.postValue(stringResource(R.string.userIsAlreadyMemberOfThisWh))
                true
            } else false
        }
    }

    private fun checkIfUserExistsInDb(invitedUserQuerySnapshot: QuerySnapshot) : Boolean {
        return if(invitedUserQuerySnapshot.isEmpty){
            _userEmailError.postValue(stringResource(R.string.userWithThisEmailDoesntExists))
            false
        } else true
    }


}