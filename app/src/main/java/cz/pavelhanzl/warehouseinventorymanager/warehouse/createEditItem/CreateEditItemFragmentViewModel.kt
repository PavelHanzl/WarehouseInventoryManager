package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateEditItemFragmentViewModel : BaseViewModel() {
    val TAG = "CreateEditItemVM"

    var createEditItemFragmentMode: String = Constants.CREATING_STRING
    var editMode = false

    //přichází v oncreate z fragmentu, pokud jedeme v editmodu
    var editedWarehouseItem = WarehouseItem()

    var warehouseId = MutableLiveData<String>("")
    var visibility = MutableLiveData<Int>(View.VISIBLE)

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    var itemProfilePhoto = MutableLiveData<ByteArray>()
    var itemPhotoUrl = MutableLiveData<String>("")

    var itemNameContent = MutableLiveData<String>("")
    var _itemNameError = MutableLiveData<String>("")
    val itemNameError: LiveData<String> get() = _itemNameError

    var itemBarcodeContent = MutableLiveData<String>("")
    var _itemBarcodeError = MutableLiveData<String>("")
    val itemBarcodeError: LiveData<String> get() = _itemBarcodeError

    var initialItemCountContent = MutableLiveData<String>("")
    var _initialItemCountError = MutableLiveData<String>("")
    val initialItemCountError: LiveData<String> get() = _initialItemCountError

    var itemPriceContent = MutableLiveData<String>("")
    var _itemPriceError = MutableLiveData<String>("")
    val itemPriceError: LiveData<String> get() = _itemPriceError

    var itemNoteContent = MutableLiveData<String>("")
    var _itemNoteError = MutableLiveData<String>("")
    val itemNoteError: LiveData<String> get() = _itemNoteError

    private val _createEditButtonEnabled = MutableLiveData<Boolean>(true)
    val createEditButtonEnabled: LiveData<Boolean> get() = _createEditButtonEnabled

    sealed class Event {
        object NavigateBack : Event()
        object NavigatePopUpBackStackToWarehouseDetail : Event()

    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun setdata(warehouseId: String) {
        this.warehouseId.value = warehouseId
        visibility.value = View.VISIBLE

    }

    fun onBackButtonClicked() {
        GlobalScope.launch { eventChannel.send(Event.NavigateBack) }
    }

    fun onCreateEditItemButtonClicked() {
        //todo dodělat

        //check validity dat
        if (!isValid()) return

        GlobalScope.launch(Dispatchers.IO) {

            //zobrazí progressbar
            _loading.postValue(true)

            //zakáže tlačítko vytvoření/editace
            _createEditButtonEnabled.postValue(false)

            try {
                var profileImageURL: Uri? = null
                val itemDocRef = db.collection("warehouses").document(warehouseId.value!!).collection("items").document()

                //pokud uživatel zvolil ve View fotku ze zařízení, tak ji nahraje na server, pokud ne, tak přeskočí
                if (itemProfilePhoto.value != null) {
                    try {
                        //nahrává na Storage
                        storage.child("/warehouses/${warehouseId.value!!}/items/${itemDocRef.id}/profileImage.jpg").putBytes(itemProfilePhoto.value!!).await()

                        //získává url nahraného souboru
                        profileImageURL = storage.child("/warehouses/${warehouseId.value!!}/items/${itemDocRef.id}/profileImage.jpg").downloadUrl.await()
                    } catch (e: Exception) {
                        Log.d(TAG, "Error: ${e.message}")
                    }
                }

                //rozdílné zapisování do databáze v závilosti jestli upravujeme současný nebo vytváříme novou položku
                if (editMode) {
                    //upravujeme
                    editItemAndSaveToDb(profileImageURL)

                    //log o úpravě
                    repoComunicationLayer.createWarehouseLogItem(
                        "Prováděny úpravy základních informací položky skladu.",
                        itemName = editedWarehouseItem.name,
                        warehouseID = editedWarehouseItem.warehouseID
                    )
                } else {
                    //vytváříme
                    createItemAndSaveToDb(itemDocRef, profileImageURL)

                    //vytvoří log k vytvářené položce
                    repoComunicationLayer.createWarehouseLogItem(
                        stringResource(R.string.newItemWasCreated),
                        itemNameContent.value!!,
                        stringResource(R.string.initialQuantity) + initialItemCountContent.value!!,
                        warehouseId.value!!
                    )
                }


            } catch (e: Exception) {
                Log.d(TAG, "Error: ${e.message}")

                //skryje progressbar
                _loading.postValue(false)

                //v případě chyby povolí tlačítko pro vytvoření/editaci, aby bylo možné akci opakovat
                _createEditButtonEnabled.postValue(true)

                return@launch
            }
            if (editMode) { // pokud jsme v edit modu tak se po úpravě chceme vrátit na detail položky, proto stačí navigateUP
                eventChannel.send(Event.NavigateBack)
            } else { // při vytváření nové položky se po vytvoření chceme vrátit do detailu skaldu
                eventChannel.send(Event.NavigatePopUpBackStackToWarehouseDetail)
            }
        }

    }

    fun isValid(): Boolean {
        var valid = true
        //vyčistí případné errory z předchozího ověření
        _itemNameError.value = ""
        _itemBarcodeError.value = ""
        _initialItemCountError.value = ""
        _itemPriceError.value = ""
        _itemNoteError.value = ""


        if (itemNameContent.value!!.isEmpty()) {
            _itemNameError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        if (itemBarcodeContent.value!!.isEmpty()) {
            _itemBarcodeError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        if (initialItemCountContent.value!!.isEmpty() || initialItemCountContent.value!!.toDouble() < 0.0) {
            _initialItemCountError.value = stringResource(R.string.typeInValueGraterOrEqualToZero)
            valid = false
        }

        if (itemPriceContent.value!!.isEmpty() || itemPriceContent.value!!.toDouble() < 0.0) {
            _itemPriceError.value = stringResource(R.string.typeInValueGraterOrEqualToZero)
            valid = false
        }

        if (itemNoteContent.value!!.length > Constants.MAX_NOTE_LENGTH) {
            _itemNoteError.value = stringResource(R.string.note_to_long)
            valid = false
        }

        return valid
    }

    private suspend fun createItemAndSaveToDb(itemDocRef: DocumentReference, profileImageURL: Uri?) {
        //vytvoří instanci položkyy
        val item = WarehouseItem()

        item.warehouseItemID = itemDocRef.id
        item.warehouseID = warehouseId.value!!
        item.name = itemNameContent.value!!
        item.note = itemNoteContent.value!!
        item.code = itemBarcodeContent.value!!
        item.photoURL = profileImageURL?.toString() ?: "" //pokud uživatel zvolil fotku, tak vyplní url, jinak zapíše prázdnej string
        item.price = itemPriceContent.value!!.toDouble()
        item.count = initialItemCountContent.value!!.toDouble()

        //zapíše do dokumentu předpřipraveného dokumentu
        itemDocRef.set(item).await()
    }

    private fun editItemAndSaveToDb(profileImageURL: Uri?) {
        //vytvoří instanci položky
        val item = WarehouseItem()

        //možné změny jsou v těchto polích
        item.name = itemNameContent.value!!
        item.note = itemNoteContent.value!!
        item.code = itemBarcodeContent.value!!
        item.price = itemPriceContent.value!!.toDouble()

        //pokud užvatel nahrál novou fotku, tak zapíše novou url, jinak nechá původní
        item.photoURL = profileImageURL?.toString() ?: editedWarehouseItem.photoURL

        //pokud se nacházíme v editmodu, tak není potřeba opět nastavovat id, proto si jej vezmeme z upravovaného objektu
        item.warehouseItemID = editedWarehouseItem.warehouseItemID

        //neměnné hodnoty, zachováváme z původního objektu
        item.createDate = editedWarehouseItem.createDate
        item.warehouseID = editedWarehouseItem.warehouseID
        item.count = editedWarehouseItem.count

        //zapíše do původního již vytvořeného dokumentu
        db.collection("warehouses").document(warehouseId.value!!).collection("items").document(item.warehouseItemID).set(item)
    }
}