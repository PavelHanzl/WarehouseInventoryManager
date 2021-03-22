package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class OwnWarehousesDetailFragmentViewModel:BaseViewModel() {
    var warehouseID = MutableLiveData<String>("a")

    fun setData(warehouseId : String){
        warehouseID.value = warehouseId
        Log.d("test", "view ${warehouseId}")
    }

}