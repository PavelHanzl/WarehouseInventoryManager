package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CreateWarehouseFragmentViewModel : BaseViewModel() {

    val TAG = "CreateWarehouseVM"

    var warehouseNameContent = MutableLiveData<String>("")
    var warehouseNoteContent = MutableLiveData<String>("")
    var warehouseProfilePhoto = MutableLiveData<ByteArray>()

    var _warehouseNameError = MutableLiveData<String>("")
    val warehouseNameError: LiveData<String> get() = _warehouseNameError

    var _warehouseNoteError = MutableLiveData<String>("")
    val warehouseNoteError: LiveData<String> get() = _warehouseNoteError

    private val _goBackToPreviousScreen = MutableLiveData<Boolean>(false)
    val goBackToPreviousScreen: LiveData<Boolean> get() = _goBackToPreviousScreen

    init {

    }

    @SuppressLint("SimpleDateFormat")
    fun onCreateButtonClicked() {

        //check validity dat
        if(!isValid())return

        GlobalScope.launch(Dispatchers.IO) {

            try {
                var profileImageURL: Uri? = null
                val warehouseDocRef = db.collection("warehouses").document()

                //pokud uživatel zvolil ve View fotku ze zařízení, tak ji nahraje na server, pokud ne, tak přeskočí
                if (warehouseProfilePhoto.value != null) {
                    try {
                        //nahrává na Storage
                        storage.child("/warehouses/${warehouseDocRef.id}/profileImage.jpg").putBytes(warehouseProfilePhoto.value!!).await()

                        //získává url nahraného souboru
                        profileImageURL = storage.child("/warehouses/${warehouseDocRef.id}/profileImage.jpg").downloadUrl.await()
                    } catch (e: Exception) {
                        Log.d(TAG, "Error: ${e.message}")
                    }
                }

                //vytvoří instanci skladu
                val warehouse = Warehouse()
                warehouse.warehouseID = warehouseDocRef.id
                warehouse.owner = auth.currentUser!!.uid
                warehouse.name = warehouseNameContent.value!!
                warehouse.note = warehouseNoteContent.value!!
                warehouse.photoURL = profileImageURL?.toString() ?: ""

                //zapíše do dokumentu
                warehouseDocRef.set(warehouse).await()

                //vytvoří log k vytvářenému skladu
                repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.warehouseCreated), warehouseID = warehouseDocRef.id)

            } catch (e: Exception) {
                Log.d(TAG, "Error: ${e.message}")
                //return@launch
            }
            _goBackToPreviousScreen.postValue(true)
        }

    }

    fun onBackButtonClicked() {
        _goBackToPreviousScreen.postValue(true)
    }

    fun isValid():Boolean{
        var valid = true
        //vyčistí případné errory z předchozího ověření
        _warehouseNameError.value = ""
        _warehouseNoteError.value = ""

        if(warehouseNameContent.value!!.isEmpty()){
            _warehouseNameError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        if( warehouseNoteContent.value!!.length>200){
            _warehouseNoteError.value = stringResource(R.string.note_to_long)
            valid = false
        }

        return valid
    }
    override fun onCleared() {
        Log.d("viewDes","destroyed1")
        super.onCleared()
    }
}