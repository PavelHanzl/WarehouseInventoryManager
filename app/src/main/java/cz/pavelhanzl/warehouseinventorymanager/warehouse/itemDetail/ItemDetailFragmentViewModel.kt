package cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ItemDetailFragmentViewModel: BaseViewModel() {
    val TAG = "ItemDetailFragmentVM"

    var visibility=MutableLiveData<Int>(View.INVISIBLE )

    private val _selectedWarehouseItem = MutableLiveData<WarehouseItem>()
    val selectedWarehouseItem: LiveData<WarehouseItem> get() = _selectedWarehouseItem

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    var _itemName = MutableLiveData<String>("")
    val itemName: LiveData<String> get() = _itemName

    var _itemBarcode = MutableLiveData<String>("")
    val itemBarcode: LiveData<String> get() = _itemBarcode

    var _itemCount = MutableLiveData<String>("")
    val itemCount: LiveData<String> get() = _itemCount

    var _itemPrice = MutableLiveData<String>("")
    val itemPrice: LiveData<String> get() = _itemPrice

    var _totalPrice = MutableLiveData<String>("")
    val totalPrice: LiveData<String> get() = _totalPrice

    var _itemNote = MutableLiveData<String>("")
    val itemNote: LiveData<String> get() = _itemNote

    var _itemProfilePhotoUrl = MutableLiveData<String>("")
    val itemProfilePhotoUrl: LiveData<String> get() = _itemProfilePhotoUrl

    lateinit var warehouseItemInDbSnapshot: Map<String, Any>
    lateinit var warehouseItemInDbDocRef: DocumentReference

    fun setdata(selectedWarehouseItem: WarehouseItem){
        this._selectedWarehouseItem.value = selectedWarehouseItem

        //ihned zobrazí data z předaného objektu z fragmentu
        setDataFromGivenWarehouseItemObject(selectedWarehouseItem)

        //následně data přenastaví na snapshot listener, aby byly live s databází, kdyby docházelo ke změnám
        setDataBasedOnDatabaseObserver(selectedWarehouseItem)



    }

    private fun setDataBasedOnDatabaseObserver(selectedWarehouseItem: WarehouseItem) {
        val warehouseItemDocRef = db.collection(Constants.WAREHOUSES_STRING).document(selectedWarehouseItem.warehouseID).collection(Constants.ITEMS_STRING).document(selectedWarehouseItem.warehouseItemID)
        warehouseItemDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                setDataFromGivenWarehouseItemObject(snapshot.toObject(WarehouseItem::class.java)!!)
                Log.d(TAG, "Current data: ${snapshot.data}")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun setDataFromGivenWarehouseItemObject(selectedWarehouseItem: WarehouseItem) {
        //setne novou (teoreticky upravenou z upravovací databáze) položku z databáze jako selected warehouse item
        _selectedWarehouseItem.value = selectedWarehouseItem

        _itemName.value = selectedWarehouseItem.name
        _itemBarcode.value = selectedWarehouseItem.code
        _itemCount.value = selectedWarehouseItem.count.toString()
        _itemPrice.value = selectedWarehouseItem.price.toString()
        _totalPrice.value = (selectedWarehouseItem.count * selectedWarehouseItem.price).toString()
        _itemNote.value = selectedWarehouseItem.note

        if(_itemNote.value!!.isEmpty()||_itemNote.value!! == ""){
            _itemNote.value = stringResource(R.string.itemHasAnyNote)
        }
        _itemProfilePhotoUrl.value=selectedWarehouseItem.photoURL
    }



    //odstraní skladovou položku z databáze
    fun deleteWarehouseItem() {
        //běží v globalscope, aby bylo možné skladovou položku obnovit ve funkci delete WhUndo i po zničení viewmodelu
        GlobalScope.launch(Dispatchers.IO) {
            makeWarehouseItemSnapshot()
            warehouseItemInDbDocRef.delete()
        }
    }

    fun undoChangesOfWarehouseItemDocument() {
        GlobalScope.launch(Dispatchers.IO) {
            warehouseItemInDbDocRef.set(warehouseItemInDbSnapshot)
        }
    }

    suspend fun makeWarehouseItemSnapshot() {
        warehouseItemInDbDocRef = db.collection(Constants.WAREHOUSES_STRING).document(selectedWarehouseItem.value!!.warehouseID).collection(Constants.ITEMS_STRING).document(selectedWarehouseItem.value!!.warehouseItemID)
        warehouseItemInDbSnapshot = warehouseItemInDbDocRef.get().await().data!!
    }


}