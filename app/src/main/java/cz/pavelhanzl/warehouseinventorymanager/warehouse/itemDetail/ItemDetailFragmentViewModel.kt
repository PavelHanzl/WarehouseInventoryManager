package cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

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
        _itemName.value = selectedWarehouseItem.name
        _itemBarcode.value = selectedWarehouseItem.code
        _itemCount.value = selectedWarehouseItem.count.toString()
        _itemPrice.value = selectedWarehouseItem.price.toString()
        _totalPrice.value = (selectedWarehouseItem.count * selectedWarehouseItem.price).toString()
        _itemNote.value = selectedWarehouseItem.note
        _itemProfilePhotoUrl.value=selectedWarehouseItem.photoURL
    }


}