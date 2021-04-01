package cz.pavelhanzl.warehouseinventorymanager.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.zxing.Result
import cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail.OwnWarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScannerFragmentViewModel : BaseViewModel() {
    lateinit var scannerMode: String

    //kontinální skenování
    var continuouslyScaning = MutableLiveData<Boolean>(false)

    val initialSpeed: Float = 1F
    val minimumSpeed: Float = 0.4F

    var scanningSpeed = MutableLiveData(initialSpeed)

    //nastavuje maximální hodnotu progressbaru v závislosti na zvolené rychlosti skenování
    val scanningMaxProgress: LiveData<Int>
        get() = if (scanningSpeed.value!!.toInt() != 0) {
            Transformations.map(scanningSpeed) {
                (it * 1000).toInt()
            }
        } else {
            Transformations.map(scanningSpeed) {
                (it + (minimumSpeed * 1000)).toInt()
            }
        }

    var _scanningProgress = MutableLiveData(initialSpeed)//prvnotní inicializace na milisekundy proto *1000
    val scanningProgress: LiveData<Int>
        get() = Transformations.map(_scanningProgress) {
            it.toInt()
        }

    // hodnota barcodu
    var _barcodeValue = MutableLiveData<String>("")
    val barcodeValue: LiveData<String> get() = _barcodeValue

    // hodnota observovaná ve view
    var scannerStartPreview = MutableLiveData<Boolean>(false)
    var scannerStopPreview = MutableLiveData<Boolean>(false)
    var barcodeScanned = MutableLiveData<Boolean>(false)

    fun scannerStartPreview() {
        scannerStartPreview.postValue(true)
    }


    fun scannerStopPreview() {
        scannerStopPreview.postValue(true)
    }

    fun scanSuccessful() {
        barcodeScanned.postValue(true)
    }

    fun decodeCode(result: Result) {
        GlobalScope.launch(Dispatchers.IO) {

            scanSuccessful()

            var counterOfScanning = _barcodeValue.value!!.toInt()
            counterOfScanning++
            _barcodeValue.postValue(counterOfScanning.toString())




            if (continuouslyScaning.value!!) {

                //nastaví countDown na čas vybraný na slideru (např. 1.5s), vynásobí 1000 abychom se dostali na ms
                var countDown = scanningSpeed.value!!.toInt() * 1000

                //pokud je scanning speed nastavená na 0, tak ji přenastaví na minimální hodnotu např 0,3s, aby byl skener použitelný
                countDown = checkIfScanSpeedIsZeroAndThenSetMinScanSpeed(countDown)

                //provede odpočítávní
                doCountDown(countDown)

                //po ukončeném odpočítávání opět zpřístupní skenner
                scannerStartPreview()

            }

        }
    }

    //po ve slideru stanovenou dobu pozastaví další skenování
    private suspend fun doCountDown(countDown: Int) {
        var countDownProgress = countDown
        while (countDownProgress > 0) {
            countDownProgress -= 10 //odečte 10ms
            _scanningProgress.postValue(countDownProgress.toFloat()) //upraví progressbar o 10ms
            delay(10) //delay 10ms
        }

        //po uběhnutí časového limitu nastaví progressbar na maximum, značící, že může začít nový sken
        _scanningProgress.postValue(scanningSpeed.value!! * 1000)
    }

    private fun checkIfScanSpeedIsZeroAndThenSetMinScanSpeed(countDown: Int): Int {
        var minimalCountDown = countDown
        if (minimalCountDown == 0) {
            minimalCountDown = (minimumSpeed * 1000).toInt()
            scanningSpeed.postValue(minimumSpeed)
            _scanningProgress.postValue(minimumSpeed * 1000)
        }
        return minimalCountDown
    }


}