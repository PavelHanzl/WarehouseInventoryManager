package cz.pavelhanzl.warehouseinventorymanager.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.zxing.Result
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.tasks.await

/**
 * Scanner fragment view model
 *
 * @constructor Create empty Scanner fragment view model
 */
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

    //zobrazuje/skrývá načítání
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    // hodnota barcodu poslední naskenované položky
    var _barcodeValue = MutableLiveData<String>(stringResource(R.string.threeDots))
    val barcodeValue: LiveData<String> get() = _barcodeValue

    //jméno poslední naskenované položky
    var _nameOfScannedBarcode = MutableLiveData<String>(stringResource(R.string.scanItem))
    val nameOfScannedBarcode: LiveData<String> get() = _nameOfScannedBarcode

    // hodnota observovaná ve view
    var scannerStartPreview = MutableLiveData<Boolean>(false)
    var scannerStopPreview = MutableLiveData<Boolean>(false)
    var barcodeScanned = MutableLiveData<Boolean>(false)

    //objekt skladu ve kterém se nacházíme
    var warehouseObject = MutableLiveData<Warehouse>()

    //list všech položek ve zvoleném skladě
    private lateinit var allItemsInDb: QuerySnapshot
    val localListOfAllItems = mutableListOf<WarehouseItem>()
    val localListOfAllItemNames = mutableListOf<String>()
    val localListOfAllItemCodes = mutableListOf<String>()

    //eventy
    sealed class Event {
        object NavigateBack : Event()
        object PlaySuccessAnimation : Event()
        object PlayErrorAnimation : Event()
        object PlayBeepSoundAndVibrate : Event()
        data class SendToast(val toastMessage: String) : Event()
        data class NonExistingItem(val scannedBarcode: String) : Event()
        //data class CreateEdit(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    /**
     * Scanner starts preview
     *
     */
    fun scannerStartPreview() {
        scannerStartPreview.postValue(true)
    }

    /**
     * Decode code
     * Decodes barcode, here main logic is happening.
     * @param result value of decoded barcode passed form fragment class
     */
    fun decodeCode(result: Result) {
        GlobalScope.launch(Dispatchers.IO) {

            //sustí zvuk pípnutí a zavibruje
            eventChannel.send(Event.PlayBeepSoundAndVibrate)

            //propíše hodnotu naskenovaného čárového kódu do UI
            _barcodeValue.postValue(result.text)

            //pokud se nenacházíme ve četcím reřimu, tak budeme zapisovat do databáze
            if (scannerMode != Constants.READING_STRING) {
                if (scannerMode == Constants.ADDING_STRING) {//přidáváme
                    runAddingRemovingTransaction(result.text.toString(), 1.0, true)
                } else { //odebíráme
                    runAddingRemovingTransaction(result.text.toString(), 1.0, false)
                }
            }

            //pokude je zaplé kontinuální skenování, pak proveď obsah těla ifu
            if (continuouslyScaning.value!!) {

                //nastaví countDown na čas vybraný na slideru (např. 1.5s), vynásobí 1000 abychom se dostali na ms
                var countDown = scanningSpeed.value!!.toInt() * 1000

                //pokud je scanning speed nastavená na 0, tak ji přenastaví na minimální hodnotu např 0,3s, aby byl skener použitelný
                countDown = checkIfScanSpeedIsZeroAndThenSetMinScanSpeed(countDown)

                //provede odpočítávní
                doCountDown(countDown)

                if (continuouslyScaning.value == true) {
                    //po ukončeném odpočítávání opět zpřístupní skenner
                    scannerStartPreview()
                }

            }

        }
    }

    /**
     * Do count down
     * Pauses the next scan for the time specified in the slider
     * @param countDown time in miliseconds
     */
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

    /**
     * Check if scan speed is zero and then set min scan speed
     * If the set value of the scan speed (slider) is set to 0, then it sets it to the minimum defined value so that it is user friendly and does not read eg 50 reads/s
     *
     * @param countDown time in miliseconds
     * @return minimal countdown in miliseconds
     */
    private fun checkIfScanSpeedIsZeroAndThenSetMinScanSpeed(countDown: Int): Int {
        var minimalCountDown = countDown
        if (minimalCountDown == 0) {
            minimalCountDown = (minimumSpeed * 1000).toInt()
            scanningSpeed.postValue(minimumSpeed)
            _scanningProgress.postValue(minimumSpeed * 1000)
        }
        return minimalCountDown
    }

    /**
     * Run adding removing transaction
     * Starts an add / remove transaction to the database
     *
     * @param code scanned barcode
     * @param count number of items to add/remove
     * @param addingMode if true then adding to db, if false then removing from db
     */
    fun runAddingRemovingTransaction(code: String, count: Double = 1.0, addingMode: Boolean = true) {

        GlobalScope.launch(Dispatchers.IO) {

            //vrátí položku skladu v závislosti na naskenovaném kódu, pokud položka s tímto kódem na skladě ještě neexistuje, tak vrátí null
            val foundWhItem = returnWarehouseItemWithGivenParameters(itemName = "", itemBarcode = code, listOfAllWarehouseItems = localListOfAllItems)

            //pokud nevrátilo null, tak položka existuje a můžeme pokročit k transakci za tímto ifem, pokud vrátila null, tak pozastavíme sken a zobrazíme možnost přidání do skladu
            if (foundWhItem != null) {
                _nameOfScannedBarcode.postValue(foundWhItem.name)
            } else {
                //přehraje animaci neúspěchu
                eventChannel.send(Event.PlayErrorAnimation)

                if (scannerMode == Constants.ADDING_STRING) { // dialog s možností vytvoření neexistující položky chceme zobrazit pouze v případě, že se nacházíme v módu přidávání, v modu odebírání nemůžeme odebrat položku co na skladě neexistuje
                    //zobrazí dialog o neexistující položce
                    eventChannel.send(Event.NonExistingItem(code))
                }

                //propíše zprávu o neexistující položce do UI
                _nameOfScannedBarcode.postValue(stringResource(R.string.itemWithThisBarcodeDoesntExists))

                //zastaví kontinuální skenování
                continuouslyScaning.postValue(false)

                //ukončí provádění této corutiny
                return@launch
            }

            try {
                _loading.postValue(true)

                //získá refenci na dokument na kterém budeme provádět transakci
                val sfDocRef = db.collection(Constants.WAREHOUSES_STRING).document(warehouseObject.value!!.warehouseID).collection(Constants.ITEMS_STRING).document(foundWhItem.warehouseItemID)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(sfDocRef)

                    var newCount: Double

                    if (addingMode) {//přidáváme
                        newCount = snapshot.getDouble(Constants.COUNT_STRING)!! + count
                    } else {//odebíráme
                        newCount = snapshot.getDouble(Constants.COUNT_STRING)!! - count

                    }

                    transaction.update(sfDocRef, Constants.COUNT_STRING, newCount)

                    // Success
                    null
                }.addOnSuccessListener {
                    _loading.postValue(false)

                    //při úspěchu provede log o operaci
                    if (addingMode) {//mód přidávání
                        repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.itemAdded), foundWhItem.name, "+$count", warehouseObject.value!!.warehouseID)
                    } else {//mód odebírání
                        repoComunicationLayer.createWarehouseLogItem(stringResource(R.string.itemRemoved), foundWhItem.name, "-$count", warehouseObject.value!!.warehouseID)
                    }

                    //přehraje animaci úspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlaySuccessAnimation) }

                }.addOnFailureListener {
                    _loading.postValue(false)

                    //přehraje animaci neúspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlayErrorAnimation) }

                }
            } catch (e: Exception) {
                _loading.postValue(false)
                //přehraje animaci neúspěchu
                GlobalScope.launch { eventChannel.send(Event.PlayErrorAnimation) }

                Log.d("Exception", e.message!!)
            }
        }
    }

    /**
     * Get list of actual warehouse items
     * Gets the actual list of warehouse items stored in actual warehouse.
     */
    suspend fun getListOfActualWarehouseItems() {

        localListOfAllItemNames.clear()
        localListOfAllItemCodes.clear()
        localListOfAllItems.clear()

        allItemsInDb = db.collection(Constants.WAREHOUSES_STRING).document(warehouseObject.value!!.warehouseID).collection(Constants.ITEMS_STRING).orderBy(Constants.NAME_STRING, Query.Direction.ASCENDING).get().await()
        for (document in allItemsInDb.documents) {
            localListOfAllItems.add(document.toObject(WarehouseItem::class.java)!!)
            localListOfAllItemNames.add(document.toObject(WarehouseItem::class.java)!!.name)
            localListOfAllItemCodes.add(document.toObject(WarehouseItem::class.java)!!.code)
        }
    }

    /**
     * Return warehouse item with given parameters
     *
     * @param itemName name of item which you want to be returned
     * @param itemBarcode barcode of item which you want to be returned
     * @param listOfAllWarehouseItems list of all warehouse items
     * @return returns found item based on given parameters or null if no item was found
     */
    private fun returnWarehouseItemWithGivenParameters(itemName: String = "", itemBarcode: String = "", listOfAllWarehouseItems: MutableList<WarehouseItem>): WarehouseItem? {
        var foundObject: WarehouseItem? = null

        listOfAllWarehouseItems.any {
            if (it.name == itemName || it.code == itemBarcode) {
                foundObject = it
                true //shoda našli jsme shodu podle jména nebo podle kódu
            } else false //neshoda nic jsme nenanšli
        }
        return foundObject
    }
}