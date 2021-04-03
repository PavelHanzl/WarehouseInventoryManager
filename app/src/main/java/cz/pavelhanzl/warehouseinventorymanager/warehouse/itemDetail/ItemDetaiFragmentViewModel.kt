package cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class ItemDetaiFragmentViewModel: BaseViewModel() {
    var itemId = MutableLiveData<String>("")
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

    fun setdata(itemId: String){
        this.itemId.value = itemId

        var warehouseItemDocRef = db.collection(Constants.WAREHOUSES_STRING)



    }


}