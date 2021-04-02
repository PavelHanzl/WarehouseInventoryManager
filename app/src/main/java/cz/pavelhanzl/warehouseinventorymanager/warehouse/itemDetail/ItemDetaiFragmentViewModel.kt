package cz.pavelhanzl.warehouseinventorymanager.warehouse.itemDetail

import android.view.View
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class ItemDetaiFragmentViewModel: BaseViewModel() {
    var itemId = MutableLiveData<String>("")
    var visibility=MutableLiveData<Int>(View.VISIBLE )



    fun setdata(itemId: String){
        this.itemId.value = itemId
        visibility.value=View.VISIBLE

    }


}