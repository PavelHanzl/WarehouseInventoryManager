package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse

import android.util.Log
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class CreateWarehouseFragmentViewModel:BaseViewModel() {

    init {

    }

    fun onCreateButtonClicked(){
        Log.d("Had", "Klikáš mi na hada, debile")
    }
}