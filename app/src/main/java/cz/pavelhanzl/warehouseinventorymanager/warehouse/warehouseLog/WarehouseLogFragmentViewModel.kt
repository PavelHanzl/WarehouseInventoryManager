package cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseLog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class WarehouseLogFragmentViewModel:BaseViewModel() {
    lateinit var warehouse: Warehouse

    var _warehouseName = MutableLiveData<String>("")
    val warehouseName: LiveData<String> get() = _warehouseName

    /**
     * Sets data
     * Prepared for future functions implementations
     * @param warehouse
     */
    fun setdata(warehouse: Warehouse) {
        this.warehouse = warehouse
        _warehouseName.value = warehouse.name
    }
}