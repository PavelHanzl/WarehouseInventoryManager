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
import java.lang.Exception

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
        object PlaySuccessAnimation: Event()
        object PlayErrorAnimation: Event()
        data class SetVisibilityOfCreateItemBtnt(val visibility: Boolean) : Event()
        //data class CreateEdit(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    //******************End of variables for AddRemoveItemFragment**********************//

    //******************Start of functions forAddRemoveItemFragment**********************//
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

    fun getListOfActualWarehouseItems() {

        dropdownMenuDataReady.postValue(false)
        localListOfAllItemNames.clear()
        localListOfAllItemCodes.clear()
        localListOfAllItems.clear()
        Log.d("Populuju", "vse smazano ted")

        allItemsInDb = db.collection("warehouses").document(warehouseID.value!!).collection("items").orderBy("name", Query.Direction.ASCENDING).get()
        allItemsInDb.addOnSuccessListener { documents ->
            for (document in documents) {
                //Log.d("položky", "${document.id} => ${document.data}")
                //naplní listy současnými položkami z databáze
                localListOfAllItems.add(document.toObject(WarehouseItem::class.java))
                localListOfAllItemNames.add(document.toObject(WarehouseItem::class.java).name)
                localListOfAllItemCodes.add(document.toObject(WarehouseItem::class.java).code)
            }

            Log.d("Populuju", "vse nahrano")
            //spustí observer ve fragmentu, který naplní dropdown menu
            dropdownMenuDataReady.postValue(true)

        }
            .addOnFailureListener { exception ->
                Log.d("položky", "Error getting documents: " + exception.message)
            }


    }

    fun onAddRemoveItemButtonClicked() {
        if (!isValid()) return

        when (addRemoveFragmentMode) {
            Constants.ADDING_STRING -> runAddingRemovingTransaction(itemBarcodeContent.value.toString(), itemCountContent.value!!.toDouble())
            Constants.REMOVING_STRING -> runAddingRemovingTransaction(itemBarcodeContent.value.toString(), itemCountContent.value!!.toDouble(), false)
        }

    }

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


        if (itemCountContent.value!!.isNotEmpty() && itemCountContent.value!!.toDouble()<=0) {
            _itemCountError.value = stringResource(R.string.typeInValueGraterThenZero)
            valid = false
        }


        return valid
    }


   fun setDropdownsBasedOnName(itemNameString: String) {
        //ověří jestli se položka s tímto názvem nachází ve skladu a případně jí vrátí
        val item = returnWarehouseItemWithGivenParameters(itemName = itemNameString, itemBarcode = "", listOfAllWarehouseItems = localListOfAllItems)

        if (item != null) {//pokud se položka nachází
            itemBarcodeContent.value = item.code //nastaví barcode odpovídající položce
            itemPhotoUrl.value = item.photoURL //nastaví fotku odpovídající položce
            GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(false)) }//skryje možnost vytvoření nové položky
            //createItemBtn.hide() //skryje možnost vytvoření nové položky
        } else {//pokud se položka nenachází
            if(addingMode) GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(true)) }//zobrazí možnost vytvoření nové položky
            //if(addingMode)createItemBtn.show() //zobrazí možnost vytvoření nové položky
            itemBarcodeContent.value = "" //vymaže hodnotu v barcode poli
            itemPhotoUrl.value = "" //nastaví defaultní obrázek

        }
    }

   fun setDropdownsBasedOnBarcode(barcodeString: String) {
        Log.d("banány", barcodeString)

        //ověří jestli se položka s tímto barcodem nachází ve skladu a případně jí vrátí
        val item = returnWarehouseItemWithGivenParameters(itemName = "", itemBarcode = barcodeString, listOfAllWarehouseItems = localListOfAllItems)

        Log.d("banány", localListOfAllItems.size.toString())
        if (item != null) {//pokud se položka nachází
            Log.d("banány", item.name)
            itemNameContent.value = item.name //nastaví název odpovídající barcodu
            itemPhotoUrl.value = item.photoURL //nastaví fotku odpovídající položce
            GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(false)) }//skryje možnost vytvoření nové položky
            //createItemBtn.hide() //skryje možnost vytvoření nové položky
        } else {//pokud se položka nenachází
            Log.d("banány", "barcode null")
            if(addingMode) GlobalScope.launch { eventChannel.send(Event.SetVisibilityOfCreateItemBtnt(true)) }//zobrazí možnost vytvoření nové položky
            //if(addingMode) createItemBtn.show() //zobrazí možnost vytvoření nové položky
            itemNameContent.value = "" //vymaže hodnotu v poli název
            itemPhotoUrl.value = "" //nastaví defaultní obrázek
        }
    }

    private fun returnWarehouseItemWithGivenParameters(itemName: String = "", itemBarcode: String = "", listOfAllWarehouseItems: MutableList<WarehouseItem>): WarehouseItem? {
        var foundObject: WarehouseItem? = null

        listOfAllWarehouseItems.any {
            if (it.name == itemName || it.code == itemBarcode) {
                foundObject = it
                Log.d("hajdin", "Nalezeno - Item:" + foundObject!!.name + " Code:" + foundObject!!.code)
                true //shoda našli jsme shodu podle jména nebo podle kódu
            } else false //neshoda nic jsme nenanšli
        }

        return foundObject

    }


    //******************End of functions  for AddRemoveItemFragment**********************//

    var warehouseID = MutableLiveData<String>("")
    var warehouseObject = MutableLiveData<Warehouse>()
    lateinit var warehouseSnapshot: Map<String, Any>

    suspend fun makeWarehouseSnapshot() {
        warehouseSnapshot = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).get().await().data!!
    }

    fun runAddingRemovingTransaction(code: String, count: Double = 1.0, addingMode: Boolean = true) {

        //todo přidat validování prázdných polí
        GlobalScope.launch(Dispatchers.IO) {

            try { //todo dořešit aby sem nedošel barkód co není v databázi, resp klidně ať dojde, ale nepokusí se zapsat, jelikož ten dokument neexistuje, zatím ošetřeno trycatchem
                _loading.postValue(true)
                val sfQueryRef = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).collection("items").whereEqualTo("code", code).limit(1).get().await()

                val sfDocRef = sfQueryRef.documents[0].reference
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
                        repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.itemAdded), itemNameContent.value.toString(), "+$count",warehouseObject.value!!.warehouseID)
                    }else{//mód odebírání
                        repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.itemRemoved), itemNameContent.value.toString(), "-$count",warehouseObject.value!!.warehouseID)
                    }

                    //todo dořešit aby se event zkonzumoval už na skenneru
                    //přehraje animaci úspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlaySuccessAnimation) }


                    Log.d("Transakce", "Transaction success!")
                }.addOnFailureListener {
                    _loading.postValue(false)

                    //přehraje animaci neúspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlayErrorAnimation) }

                    Log.d("Transakce", "Transaction failure!!!!!!!!!!"+ it.message)
                }
            } catch (e: Exception){
                Log.d("EXCEPTION", e.message!!)
            }


        }


    }

    fun undoChangesOfWarehouseDocument() {
        GlobalScope.launch(Dispatchers.IO) {
            db.collection("warehouses").document(warehouseObject.value!!.warehouseID).set(warehouseSnapshot)
        }
    }

    //odstraní sklad z databáze
    fun deleteWarehouse() {
        //běží v globalscope, aby bylo možné sklad obnovit ve funkci delete WhUndo i po zničení viewmodelu
        GlobalScope.launch(Dispatchers.IO) {
            makeWarehouseSnapshot()
            db.collection("warehouses").document(warehouseObject.value!!.warehouseID).delete()
        }
    }

    //odstraní přihlášeného uživatele z uživatelů skladu. Již se přihlášenmu uživateli nebude zobrazovat v sekci "ostatní sklady".
    fun leaveWarehouse() {
        GlobalScope.launch(Dispatchers.IO) {
            makeWarehouseSnapshot()
            db.collection("warehouses").document(warehouseObject.value!!.warehouseID).update("users", FieldValue.arrayRemove(auth.currentUser!!.uid))
        }
    }

    fun getWarehouseOwner(warehouseId: String): Task<DocumentSnapshot> {
        return db.collection("warehouses").document(warehouseId).get()
    }

    //nastaví data při creatu view k tomuto viewmodelu přiřazenému
    fun setData(warehouseId: String) {
        warehouseID.value = warehouseId

        val warehouseRef = db.collection("warehouses").document(warehouseId)
        warehouseRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("WH-Detail", "Listen failed.", error)
                return@addSnapshotListener
            }


            if (snapshot != null && snapshot.exists()) {
                Log.d("WH-Detail", "Current data: ${snapshot.data}")
                warehouseObject.value = snapshot.toObject<Warehouse>()!!
                Log.d("WH-Detail", "Current data: ${warehouseObject.value!!.name}")
            } else {
                Log.d("WH-Detail", "Current data: null")
            }

        }


    }

    fun onBackButtonClicked() {
        GlobalScope.launch { eventChannel.send(Event.NavigateBack) }

    }

    override fun onCleared() {
        Log.d("wipe", "wiped")
        super.onCleared()
    }

}