package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
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
    var editMode = false
    var edittedWarehouse: Warehouse = Warehouse()

    var warehouseNameContent = MutableLiveData<String>("")
    var warehouseNoteContent = MutableLiveData<String>("")
    var warehouseProfilePhoto = MutableLiveData<ByteArray>()

    var _warehouseNameError = MutableLiveData<String>("")
    val warehouseNameError: LiveData<String> get() = _warehouseNameError

    var _warehouseNoteError = MutableLiveData<String>("")
    val warehouseNoteError: LiveData<String> get() = _warehouseNoteError

    private val _goBackToPreviousScreen = MutableLiveData<Boolean>(false)
    val goBackToPreviousScreen: LiveData<Boolean> get() = _goBackToPreviousScreen

    private val _visibilityOfProgressIndicator = MutableLiveData<Int>(View.GONE)
    val visibiliyOfProgressIndicator: LiveData<Int> get() = _visibilityOfProgressIndicator

    private val _createEditButtonEnabled = MutableLiveData<Boolean>(true)
    val createEditButtonEnabled: LiveData<Boolean> get() = _createEditButtonEnabled


    @SuppressLint("SimpleDateFormat")
    fun onCreateButtonClicked() {

        //check validity dat
        if (!isValid()) return

        GlobalScope.launch(Dispatchers.IO) {

            //zobrazí progressbar
            _visibilityOfProgressIndicator.postValue(View.VISIBLE)

            //zakáže tlačítko vytvoření/editace
            _createEditButtonEnabled.postValue(false)

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

                //rozdílné zapisování do databáze v závilosti jestli upravujeme současný nebo vytváříme nový sklad
                if (editMode) {
                    //upravujeme
                    editWarehouseAndSaveToDb(profileImageURL)

                    //log o úpravě
                    repoComunicationLayer.createWarehouseLogItem("Prováděny úpravy základních informací o skladu.", warehouseID = edittedWarehouse.warehouseID)
                } else {
                    //vytváříme
                    createWarehouseAndSaveToDb(warehouseDocRef, profileImageURL)

                    //vytvoří log k vytvářenému skladu
                    repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.warehouseCreated), warehouseID = warehouseDocRef.id)
                }


            } catch (e: Exception) {
                Log.d(TAG, "Error: ${e.message}")

                //skryje progressbar
                _visibilityOfProgressIndicator.postValue(View.GONE)

                //v případě chyby povolí tlačítko pro vytvoření/editaci, aby bylo možné akci opakovat
                _createEditButtonEnabled.postValue(true)

                return@launch
            }
            _goBackToPreviousScreen.postValue(true)
        }

    }

    private fun editWarehouseAndSaveToDb(profileImageURL: Uri?) {
        //vytvoří instanci skladu
        val warehouse = Warehouse()

        //možné změny jsou v těchto polích
        warehouse.name = warehouseNameContent.value!!
        warehouse.note = warehouseNoteContent.value!!

        //pokud užvatel nahrál novou fotku, tak zapíše novou url, jinak nechá původní
        warehouse.photoURL = profileImageURL?.toString() ?: edittedWarehouse.photoURL

        //pokud se nacházíme v editmodu, tak není potřeba opět nastavovat id, proto si jej vezmeme z upravovaného objektu
        warehouse.warehouseID = edittedWarehouse.warehouseID

        //neměnné hodnoty, zachováváme z původního objektu
        warehouse.owner = edittedWarehouse.owner
        warehouse.createDate = edittedWarehouse.createDate
        warehouse.users = edittedWarehouse.users

        //zapíše do původního již vytvořeného dokumentu
        db.collection("warehouses").document(warehouse.warehouseID).set(warehouse)
    }

    private suspend fun createWarehouseAndSaveToDb(warehouseDocRef: DocumentReference, profileImageURL: Uri?) {
        //vytvoří instanci skladu
        val warehouse = Warehouse()

        warehouse.warehouseID = warehouseDocRef.id
        warehouse.owner = auth.currentUser!!.uid
        warehouse.name = warehouseNameContent.value!!
        warehouse.note = warehouseNoteContent.value!!
        warehouse.photoURL = profileImageURL?.toString() ?: "" //pokud uživatel zvolil fotku, tak vyplní url, jinak zapíše prázdnej string

        //zapíše do dokumentu předpřipraveného dokumentu
        warehouseDocRef.set(warehouse).await()
    }

    fun onBackButtonClicked() {
        _goBackToPreviousScreen.postValue(true)
    }

    fun isValid(): Boolean {
        var valid = true
        //vyčistí případné errory z předchozího ověření
        _warehouseNameError.value = ""
        _warehouseNoteError.value = ""

        if (warehouseNameContent.value!!.isEmpty()) {
            _warehouseNameError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        if (warehouseNoteContent.value!!.length > 200) {
            _warehouseNoteError.value = stringResource(R.string.note_to_long)
            valid = false
        }

        return valid
    }

}