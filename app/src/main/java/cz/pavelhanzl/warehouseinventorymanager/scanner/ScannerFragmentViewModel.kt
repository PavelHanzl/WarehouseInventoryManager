package cz.pavelhanzl.warehouseinventorymanager.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel

class ScannerFragmentViewModel: BaseViewModel() {
    var _barcodeValue = MutableLiveData<String>("")
    val barcodeValue: LiveData<String> get() = _barcodeValue



}