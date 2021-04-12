package cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
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
 * Warehouses detail fragment view model
 *
 * @constructor Create empty Warehouses detail fragment view model
 */
class WarehousesDetailFragmentViewModel : BaseViewModel() {

    //******************Start of variables forAddRemoveItemFragment**********************//

    lateinit var addRemoveFragmentMode: String
    var addingMode = false

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    var itemPhotoUrl = MutableLiveData<String>("")

    var itemNameContent = MutableLiveData<String>("")
    var _itemNameError = MutableLiveData<String>("")
    val itemNameError: LiveData<String> get() = _itemNameError

    var itemBarcodeContent = MutableLiveData<String>("")
    var _itemBarcodeError = MutableLiveData<String>("")
    val itemBarcodeError: LiveData<String> get() = _itemBarcodeError

    var itemCountContent = MutableLiveData<String>("")
    var _itemCountError = MutableLiveData<String>("")
    val itemCountError: LiveData<String> get() = _itemCountError

    private val _addRemoveButtonEnabled = MutableLiveData<Boolean>(true)
    val addRemoveButtonEnabled: LiveData<Boolean> get() = _addRemoveButtonEnabled

    private lateinit var allItemsInDb: Task<QuerySnapshot>
    val localListOfAllItems = mutableListOf<WarehouseItem>()
    val localListOfAllItemNames = mutableListOf<String>()
    val localListOfAllItemCodes = mutableListOf<String>()
    val dropdownMenuDataReady = MutableLiveData<Boolean>(false)


    sealed class Event {
        object NavigateBack : Event()
        object PlaySuccessAnimation : Event()
        object PlayErrorAnimation : Event()
        data class SetVisibilityOfCreateItemBtnt(val visibility: Boolean) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    //******************End of variables for AddRemoveItemFragment**********************//

    //******************Start of functions forAddRemoveItemFragment**********************//

    /**
     * Init variables for add remove fragment (wipes data of this viewmodel)
     */
    fun initVariablesForAddRemoveFragment() {
        _loading.value = false

        itemPhotoUrl.value = ""

        itemNameContent.value = ""
        _itemNameError.value = ""

        itemBarcodeContent.value = ""
        _itemBarcodeError.value = ""

        itemCountContent.value = ""
        _itemCountError.value = ""

        _addRemoveButtonEnabled.value = true

    }

    /**
     * Gets list of actual warehouse items that are in the warehouse at the moment of calling this function
     *
     */
    fun getListOfActualWarehouseItems() {

        dropdownMenuDataReady.postValue(false)
        localListOfAllItemNames.clear()
        localListOfAllItemCodes.clear()
        localListOfAllItems.clear()

        allItemsInDb = db.collection(Constants.WAREHOUSES_STRING).document(warehouseID.value!!).collection(Constants.ITEMS_STRING).orderBy(Constants.NAME_STRING, Query.Direction.ASCENDING).get()
        allItemsInDb.addOnSuccessListener { documents ->
            for (document in documents) {
                //naplní listy současnými položkami z databáze
                localListOfAllItems.add(document.toObject(WarehouseItem::class.java))
                localListOfAllItemNames.add(document.toObject(WarehouseItem::class.java).name)
                localListOfAllItemCodes.add(document.toObject(WarehouseItem::class.java).code)
            }

            //spustí observer ve fragmentu, který naplní dropdown menu
            dropdownMenuDataReady.postValue(true)

        }
            .addOnFailureListener { exception ->
                Log.d("Exception", "Error getting documents: " + exception.message)
            }
    }

    /**
     * Defines what should happen when add or remove item button is clicked
     */
    fun onAddRemoveItemButtonClicked() {
        if (!isValid()) return

        when (addRemoveFragmentMode) {
            Constants.ADDING_STRING -> runAddingRemovingTransaction(itemBarcodeContent.value.toString(), itemCountContent.value!!.toDouble())
            Constants.REMOVING_STRING -> runAddingRemovingTransaction(itemBarcodeContent.value.toString(), itemCountContent.value!!.toDouble(), false)
        }

    }

    /**
     * Is valid
     * validates all given inputs
     *
     * @return returns true if valid
     */
    fun isValid(): Boolean {
        var valid = true
        //vyčistí případné errory z předchozího ověření
        _itemNameError.value = ""
        _itemBarcodeError.value = ""
        _itemCountError.value = ""

        if (itemNameContent.value!!.isEmpty()) {
            _itemNameError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        if (itemBarcodeContent.value!!.isEmpty()) {
            _itemBarcodeError.value = stringResource(R.string.type_in_name)
            valid = false
        }

        if (itemCountContent.value!!.isEmpty()) {
            _itemCountError.value = stringResource(R.string.type_in_name)
            valid = false
        }


        if (itemCountContent.value!!.isNotEmpty() && itemCountContent.value!!.toDouble() <= 0) {
            _itemCountError.value = stringResource(R.string.typeInValueGraterThenZero)
            valid = false
        }


        return valid
    }

    /**
     * Sets dropdowns based on name
     * if it finds match in given name and name in database, then it automatically fills in corresponding barcode to the second dropdown
     *
     * @param itemNameString item name to compare/find
     */
    fun setDropdownsBasedOnName(itemNameString: String) {
        //ověří jestli se položka s tímto názvem nachází ve skladu a případně jí vrátí
        val item = returnWarehouseItemWithGivenParameters(itemName = itemNameString, itemBarcode = "", listOfAllWarehouseItems = localListOfAllItems)

        if (item != null) {//pokud se položka nachází
            itemBarcodeContent.value = item.code //nastaví barcode odpovídající položce
            itemPhotoUrl.value = item.photoURL //nastaví fotku odpovídající položce
            GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(false)) }//skryje možnost vytvoření nové položky
        } else {//pokud se položka nenachází
            if (addingMode) GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(true)) }//zobrazí možnost vytvoření nové položky
            itemBarcodeContent.value = "" //vymaže hodnotu v barcode poli
            itemPhotoUrl.value = "" //nastaví defaultní obrázek

        }
    }

    /**
     * Set dropdowns based on barcode
     * if it finds match in given barcode and barcode in database, then it automatically fills in corresponding name to the second dropdown
     * @param barcodeString item barocde to compare/find
     */
    fun setDropdownsBasedOnBarcode(barcodeString: String) {

        //ověří jestli se položka s tímto barcodem nachází ve skladu a případně jí vrátí
        val item = returnWarehouseItemWithGivenParameters(itemName = "", itemBarcode = barcodeString, listOfAllWarehouseItems = localListOfAllItems)

        if (item != null) {//pokud se položka nachází
            itemNameContent.value = item.name //nastaví název odpovídající barcodu
            itemPhotoUrl.value = item.photoURL //nastaví fotku odpovídající položce
            GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(false)) }//skryje možnost vytvoření nové položky
        } else {//pokud se položka nenachází
            if (addingMode) GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(true)) }//zobrazí možnost vytvoření nové položky
            itemNameContent.value = "" //vymaže hodnotu v poli název
            itemPhotoUrl.value = "" //nastaví defaultní obrázek
        }
    }

    /**
     * Return warehouse item with given parameters
     *
     * @param itemName name of item which you want to be returned
     * @param itemBarcode barcode of item which you want to be returned
     * @param listOfAllWarehouseItems list of all warehouse items
     * @return returns found item based on given parameters or null if no item was found
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

    //******************End of functions  for AddRemoveItemFragment**********************//

    var warehouseID = MutableLiveData<String>("")
    var warehouseObject = MutableLiveData<Warehouse>()
    lateinit var warehouseSnapshot: Map<String, Any>

    /**
     * Makes warehouse snapshot of currently deleted warehouse, so we can restore them if user pres undo button in snackbar
     *
     */
    suspend fun makeWarehouseSnapshot() {
        warehouseSnapshot = db.collection(Constants.WAREHOUSES_STRING).document(warehouseObject.value!!.warehouseID).get().await().data!!
    }

    /**
     * Run adding removing transaction
     * Starts an add / remove transaction to the database
     *
     * @param code scanned barcode
     * @param count number of items to add/remove
     * @param addingMode if true then adding to db, if false then removing from db
     */
    fun runAddingRemovingTransaction(code: String, count: Double = 1.0, addingMode: Boolean = true) {

        GlobalScope.launch(Dispatchers.IO) {
            //zde vždy přijde barcode položky, která v databázi existuje (ošetřeno na frontendu), pak můžeme předávat rovnou id itemy, díky čemuž je celá operace rychlejší a úspěšnější
            var foundWhItem = returnWarehouseItemWithGivenParameters(itemName = "", itemBarcode = code, listOfAllWarehouseItems = localListOfAllItems)

            try {
                _loading.postValue(true)
                //val sfQueryRef = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).collection("items").whereEqualTo("code", code).limit(1).get().await()
                //val sfDocRef = sfQueryRef.documents[0].reference

                val sfDocRef =  db.collection(Constants.WAREHOUSES_STRING).document(warehouseObject.value!!.warehouseID).collection(Constants.ITEMS_STRING).document(foundWhItem!!.warehouseItemID)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(sfDocRef)

                    var newCount: Double

                    if (addingMode) {//přidáváme
                        newCount = snapshot.getDouble("count")!! + count
                    } else {//odebíráme
                        newCount = snapshot.getDouble("count")!! - count

                    }

                    transaction.update(sfDocRef, "count", newCount)

                    // Success
                    null
                }.addOnSuccessListener {
                    _loading.postValue(false)

                    //při úspěchu provede log o operaci
                    if (addingMode) {//mód přidávání
                        repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.itemAdded), itemNameContent.value.toString(), "+$count", warehouseObject.value!!.warehouseID)
                    } else {//mód odebírání
                        repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.itemRemoved), itemNameContent.value.toString(), "-$count", warehouseObject.value!!.warehouseID)
                    }


                    //přehraje animaci úspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlaySuccessAnimation) }

                }.addOnFailureListener {
                    _loading.postValue(false)

                    //přehraje animaci neúspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlayErrorAnimation) }

                }
            } catch (e: Exception) {
                _loading.postValue(false)
                Log.d("Exception", e.message!!)
            }
        }
    }

    /**
     * Undo changes of warehouse document
     * takes previously made warehouse snapshot and sets it in to the db, leading to restore deleted warehouse
     */
    fun undoChangesOfWarehouseDocument() {
        GlobalScope.launch(Dispatchers.IO) {
            db.collection(Constants.WAREHOUSES_STRING).document(warehouseObject.value!!.warehouseID).set(warehouseSnapshot)
        }
    }

    /**
     * Delete warehouse
     * delete current warehouse and invitations to this warehouse
     */
    fun deleteWarehouse() {
        //běží v globalscope, aby bylo možné sklad obnovit ve funkci delete WhUndo i po zničení viewmodelu
        GlobalScope.launch(Dispatchers.IO) {
            makeWarehouseSnapshot()
            //odstraní sklad
            db.collection(Constants.WAREHOUSES_STRING).document(warehouseObject.value!!.warehouseID).delete()

            //odstraní všechny pozvánky do tohoto skladu
            var query = db.collection(Constants.INVITATIONS_STRING).whereEqualTo("warehouseId", warehouseObject.value!!.warehouseID)

            val batch = db.batch()
            query.get().await().forEach {
                batch.delete(it.reference)
            }
            batch.commit()
        }
    }

    /**
     * Leave warehouse
     * Removes the logged in user from the warehouse users. He or she will not have this wh in the "other warehouses" section anymore.
     */
    fun leaveWarehouse() {
        GlobalScope.launch(Dispatchers.IO) {
            makeWarehouseSnapshot()
            db.collection(Constants.WAREHOUSES_STRING).document(warehouseObject.value!!.warehouseID).update(Constants.USERS_STRING, FieldValue.arrayRemove(auth.currentUser!!.uid))
        }
    }



    /**
     * Sets data
     * intialy sets the data for this viewmodel, function called from fragment class
     * @param warehouseId
     */
    fun setData(warehouseId: String) {
        warehouseID.value = warehouseId

        val warehouseRef = db.collection(Constants.WAREHOUSES_STRING).document(warehouseId)
        warehouseRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Exception", "Listen failed.", error)
                return@addSnapshotListener
            }


            if (snapshot != null && snapshot.exists()) {
                Log.d("Exception", "Current data: ${snapshot.data}")
                warehouseObject.value = snapshot.toObject<Warehouse>()!!
                Log.d("Exception", "Current data: ${warehouseObject.value!!.name}")
            } else {
                Log.d("Exception", "Current data: null")
            }

        }


    }

    /**
     * On back button clicked
     * handles on back button clicked, returns to previous fragment
     */
    fun onBackButtonClicked() {
        GlobalScope.launch { eventChannel.send(Event.NavigateBack) }
    }

}