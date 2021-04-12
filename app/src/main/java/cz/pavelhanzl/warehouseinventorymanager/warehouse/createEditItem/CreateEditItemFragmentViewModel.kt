package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
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

/**
 * Create edit item fragment view model
 *
 * @constructor Create empty Create edit item fragment view model
 */
class CreateEditItemFragmentViewModel : BaseViewModel() {
    val TAG = "CreateEditItemVM"

    var createEditItemFragmentMode: String = Constants.CREATING_STRING
    var editMode = false
    var valid = true

    //přichází v oncreate z fragmentu, pokud jedeme v editmodu
    var editedWarehouseItem = WarehouseItem()

    lateinit var warehouseId: String
    private val localListOfAllItems = mutableListOf<WarehouseItem>()

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

    /**
     * Sets data
     *
     * @param warehouseId id of actual warehouse
     */
    fun setdata(warehouseId: String) {
        this.warehouseId = warehouseId
        getListOfActualWarehouseItems()

    }

    /**
     * Gets list of actual warehouse items
     */
    private fun getListOfActualWarehouseItems() {

        localListOfAllItems.clear()

        val warehouseItemsCollection = db.collection(Constants.WAREHOUSES_STRING).document(warehouseId).collection(Constants.ITEMS_STRING).get()
        warehouseItemsCollection.addOnSuccessListener { documents ->
            for (document in documents) {
                //naplní list současnými položkami z databáze
                localListOfAllItems.add(document.toObject(WarehouseItem::class.java))
            }
        }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: " + exception.message)
            }


    }

    /**
     * Returns warehouse item with given parameters
     *
     * @param itemName name of item you want to get
     * @param itemBarcode barcode of item you want to get
     * @param listOfAllWarehouseItems list of all items in actual
     * @return returns warehouse based on given parameters
     */
    private fun returnWarehouseItemWithGivenParameters(itemName: String = "", itemBarcode: String = "", listOfAllWarehouseItems: MutableList<WarehouseItem>): WarehouseItem? {
        var foundObject: WarehouseItem? = null

        listOfAllWarehouseItems.any {
            if (it.name == itemName || it.code == itemBarcode) {
                foundObject = it
                true //shoda našli jsme shodu podle jména nebo podle kódu
            } else false //neshoda nic jsme nenanšli
        }
        return foundObject
    }

    /**
     * On back button clicked
     * navigates to the previous location
     */
    fun onBackButtonClicked() {
        GlobalScope.launch { eventChannel.send(Event.NavigateBack) }
    }

    /**
     * On create edit item button clicked
     * creates or edits a warehouse item
     */
    fun onCreateEditItemButtonClicked() {

        if (!isValid()) return //pokud není valid, tak nic nedělej
        GlobalScope.launch(Dispatchers.IO) {

            //zobrazí progressbar
            _loading.postValue(true)

            //zakáže tlačítko vytvoření/editace
            _createEditButtonEnabled.postValue(false)

            try {
                var profileImageURL: Uri? = null
                val itemDocRef = db.collection(Constants.WAREHOUSES_STRING).document(warehouseId).collection(Constants.ITEMS_STRING).document()

                //pokud uživatel zvolil ve View fotku ze zařízení, tak ji nahraje na server, pokud ne, tak přeskočí
                if (itemProfilePhoto.value != null) {
                    try {
                        //nahrává na Storage
                        storage.child("/warehouses/${warehouseId}/items/${itemDocRef.id}/profileImage.jpg").putBytes(itemProfilePhoto.value!!).await()

                        //získává url nahraného souboru
                        profileImageURL = storage.child("/warehouses/${warehouseId}/items/${itemDocRef.id}/profileImage.jpg").downloadUrl.await()
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
                        stringResource(R.string.modificationsOfBasicInformationWereMade),
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
                        warehouseId
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

    /**
     * Checks if all inputs are valid
     *
     * @return returns true if valid
     */
    fun isValid(): Boolean {
        valid = true

        //vyčistí případné errory z předchozího ověření
        _itemNameError.value = ""
        _itemBarcodeError.value = ""
        _initialItemCountError.value = ""
        _itemPriceError.value = ""
        _itemNoteError.value = ""

        //musí být před samotnou kontrolou na prázdné pole, jinak by u prázdného pole nevyhodilo žádnou hlášku
        checkIfThereIsNoItemWithSameNameInWH()

        if (itemNameContent.value!!.isEmpty()) {
            _itemNameError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        //musí být před samotnou kontrolou na prázdné pole, jinak by u prázdného pole nevyhodilo žádnou hlášku
        checkIfThereIsNoItemWithSameBarcodeInWH()

        if (itemBarcodeContent.value!!.isEmpty()) {
            _itemBarcodeError.value = stringResource(R.string.type_in_name)
            valid = false
        }



        if (initialItemCountContent.value!!.isEmpty() || (initialItemCountContent.value!!.toDouble() < 0.0 && !editMode)) { //pokud je skladová položka v mínusu, tak do tohoto stavu standatní operací "vyskladnění" a je zaznamenána v logu, v editmodu tedy chceme pustit přes validaci záporné množství, ale nedovolujeme to při vytváření
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

    /**
     * Checks if there is no item with same barcode in warehouse
     *
     * @param barcode barcode to compare
     */
    fun checkIfThereIsNoItemWithSameBarcodeInWH(barcode: String = itemBarcodeContent.value!!) {

            //položka se stejným čárovým kódem již existuje
            val existingWarehouseItem = returnWarehouseItemWithGivenParameters(itemName = "", itemBarcode = barcode, listOfAllWarehouseItems = localListOfAllItems)
            if (existingWarehouseItem !=  null && existingWarehouseItem!!.code != editedWarehouseItem.code) {
                _itemBarcodeError.value = stringResource(R.string.itemWithThisBarcodeAreadyExistsPart1) + existingWarehouseItem.name + stringResource(R.string.itemWithThisBarcodeAreadyExistsPart2)
                valid = false
            } else {
               _itemBarcodeError.value = ""
            }

    }

    /**
     * Checks if there is no item with same name in warehouse
     *
     * @param name name to compare
     */
    fun checkIfThereIsNoItemWithSameNameInWH(name: String = itemNameContent.value!!) {

        //položka se stejným názvem již existuje
        val existingWarehouseItem = returnWarehouseItemWithGivenParameters(itemName = name, itemBarcode = "", listOfAllWarehouseItems = localListOfAllItems)
        if (existingWarehouseItem != null && existingWarehouseItem!!.name != editedWarehouseItem.name) {
            _itemNameError.value = stringResource(R.string.itemWithZhisNameAlreadyExists)
            valid = false
        } else {
            _itemNameError.value = ""
        }

    }

    /**
     * Creates item and saves it to db
     *
     * @param itemDocRef item document reference to which you will be writing
     * @param profileImageURL url of image
     */
    private suspend fun createItemAndSaveToDb(itemDocRef: DocumentReference, profileImageURL: Uri?) {
        //vytvoří instanci položkyy
        val item = WarehouseItem()

        item.warehouseItemID = itemDocRef.id
        item.warehouseID = warehouseId
        item.name = itemNameContent.value!!
        item.name_lowercase = itemNameContent.value!!.toLowerCase()
        item.note = itemNoteContent.value!!
        item.code = itemBarcodeContent.value!!
        item.photoURL = profileImageURL?.toString() ?: "" //pokud uživatel zvolil fotku, tak vyplní url, jinak zapíše prázdnej string
        item.price = itemPriceContent.value!!.toDouble()
        item.count = initialItemCountContent.value!!.toDouble()

        //zapíše do dokumentu předpřipraveného dokumentu
        itemDocRef.set(item).await()
    }

    /**
     * Edit item and save to db
     *
     * @param profileImageURL url of image
     */
    private fun editItemAndSaveToDb(profileImageURL: Uri?) {
        //vytvoří instanci položky
        val item = WarehouseItem()

        //možné změny jsou v těchto polích
        item.name = itemNameContent.value!!
        item.name_lowercase = itemNameContent.value!!.toLowerCase()
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
        db.collection(Constants.WAREHOUSES_STRING).document(warehouseId).collection(Constants.ITEMS_STRING).document(item.warehouseItemID).set(item)
    }
}