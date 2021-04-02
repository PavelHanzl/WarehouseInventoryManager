package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CreateEditItemFragmentViewModel:BaseViewModel() {
    var createEditItemFragmentMode: String = Constants.CREATING_STRING
    var createMode = false

    var warehouseId = MutableLiveData<String>("")
    var visibility= MutableLiveData<Int>(View.VISIBLE )




    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    var itemPhotoUrl = MutableLiveData<String>("")

    var itemNameContent = MutableLiveData<String>("")
    var _itemNameError = MutableLiveData<String>("")
    val itemNameError: LiveData<String> get() = _itemNameError

    var itemBarcodeContent = MutableLiveData<String>("")
    var _itemBarcodeError = MutableLiveData<String>("")
    val itemBarcodeError: LiveData<String> get() = _itemBarcodeError

    var initialItemCountContent = MutableLiveData<String>("")
    var _initialItemCountError = MutableLiveData<String>("")
    val initialItemCountError: LiveData<String> get() = _initialItemCountError

    var itemPriceContent = MutableLiveData<String>("")
    var _itemPriceError = MutableLiveData<String>("")
    val itemPriceError: LiveData<String> get() = _itemPriceError

    var itemNoteContent = MutableLiveData<String>("")
    var _itemNoteError = MutableLiveData<String>("")
    val itemNoteError: LiveData<String> get() = _itemNoteError

    private val _createEditButtonEnabled = MutableLiveData<Boolean>(true)
    val createEditButtonEnabled: LiveData<Boolean> get() = _createEditButtonEnabled



    sealed class Event {
        object NavigateBack : Event()

    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()






    fun setdata(warehouseId: String){
        this.warehouseId.value = warehouseId
        visibility.value= View.VISIBLE

    }

    fun onBackButtonClicked() {
        GlobalScope.launch { eventChannel.send(CreateEditItemFragmentViewModel.Event.NavigateBack) }
    }

    fun onCreateEditItemButtonClicked(){
        //todo dodělat
    }

}