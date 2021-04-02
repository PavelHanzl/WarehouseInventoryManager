package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem

import android.view.View
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class CreateEditItemViewModel:BaseViewModel() {
    var warehouseId = MutableLiveData<String>("")
    var visibility= MutableLiveData<Int>(View.VISIBLE )


    fun setdata(warehouseId: String){
        this.warehouseId.value = warehouseId
        visibility.value= View.VISIBLE

    }

}