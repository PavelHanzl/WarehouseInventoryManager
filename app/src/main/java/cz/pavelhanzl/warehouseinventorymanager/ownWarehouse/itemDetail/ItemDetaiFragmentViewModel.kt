package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.itemDetail

import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class ItemDetaiFragmentViewModel: BaseViewModel() {
    var itemId = MutableLiveData<String>("")


    fun setdata(itemId: String){
        this.itemId.value = itemId
    }


}