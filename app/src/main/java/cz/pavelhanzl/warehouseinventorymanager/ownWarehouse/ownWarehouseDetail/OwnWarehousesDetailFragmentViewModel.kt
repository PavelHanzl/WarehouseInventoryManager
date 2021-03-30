package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import cz.pavelhanzl.warehouseinventorymanager.repository.User
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OwnWarehousesDetailFragmentViewModel : BaseViewModel() {

    //******************Start of variables forAddRemoveItemFragment**********************//

    lateinit var addRemoveFragmentMode: String

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

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

    sealed class Event {
        object NavigateBack : Event()
        //data class CreateEdit(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    //******************End of variables for AddRemoveItemFragment**********************//

    //******************Start of functions forAddRemoveItemFragment**********************//
    fun initVariablesForAddRemoveFragment() {
        _loading.value = false
        _itemNameError.value = ""

        itemBarcodeContent.value = ""
        _itemBarcodeError.value = ""

        itemCountContent.value = ""
        _itemCountError.value = ""

        _addRemoveButtonEnabled.value = true

        localListOfAllItemNames.clear()
        localListOfAllItems.clear()

    }

    fun getListOfActualWarehouseItems() {
        allItemsInDb = db.collection("warehouses").document(warehouseID.value!!).collection("items").orderBy("name", Query.Direction.ASCENDING).get()
        allItemsInDb.addOnSuccessListener { documents ->
            for (document in documents) {
                //Log.d("položky", "${document.id} => ${document.data}")

                localListOfAllItems.add(document.toObject(WarehouseItem::class.java))
                localListOfAllItemNames.add(document.toObject(WarehouseItem::class.java).name)
            }
        }
            .addOnFailureListener { exception ->
                Log.d("položky", "Error getting documents: " + exception.message)
            }
    }

    fun onAddRemoveItemButtonClicked() {

        //todo zbyva implementovat

    }
    //******************End of functions  for AddRemoveItemFragment**********************//

    var warehouseID = MutableLiveData<String>("")
    var warehouseObject = MutableLiveData<Warehouse>()
    lateinit var warehouseSnapshot: Map<String, Any>

    suspend fun makeWarehouseSnapshot() {
        warehouseSnapshot = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).get().await().data!!
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