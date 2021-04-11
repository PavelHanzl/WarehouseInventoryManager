package cz.pavelhanzl.warehouseinventorymanager.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.zxing.Result
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.repository.WarehouseItem
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import cz.pavelhanzl.warehouseinventorymanager.stringResource
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.tasks.await

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

    // hodnota barcodu
    var _barcodeValue = MutableLiveData<String>("")
    val barcodeValue: LiveData<String> get() = _barcodeValue

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
        data class SendToast(val toastMessage: String) : Event()
        data class NonExistingItem(val scannedBarcode: String) : Event()
        //data class CreateEdit(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

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

                if(continuouslyScaning.value == true){
                //po ukončeném odpočítávání opět zpřístupní skenner
                scannerStartPreview()
                }

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

    fun runAddingRemovingTransaction(code: String, count: Double = 1.0, addingMode: Boolean = true) {

        GlobalScope.launch(Dispatchers.IO) {

            //vrátí položku skladu v závislosti na naskenovaném kódu, pokud položka s tímto kódem na skladě ještě neexistuje, tak vrátí null
            var foundWhItem = returnWarehouseItemWithGivenParameters(itemName = "", itemBarcode = code, listOfAllWarehouseItems = localListOfAllItems)

            //pokud nevrátilo null, tak položka existuje a můžeme pokročit k transakci za tímto ifem, pokud vrátila null, tak pozastavíme sken a zobrazíme možnost přidání do skladu
            if( foundWhItem != null){
                eventChannel.send(Event.SendToast(foundWhItem.name))
            }else{
                //přehraje animaci neúspěchu
                 eventChannel.send(Event.PlayErrorAnimation)

                //zobrazí dialog o neexistující položce
                eventChannel.send(Event.NonExistingItem(code))

                //zastaví kontinuální skenování
                continuouslyScaning.postValue(false)

                //ukončí provádění této corutiny
                return@launch
            }



            try {
                _loading.postValue(true)


                //val querySnapshot = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).collection("items").whereEqualTo("code", code).limit(1).get().await()
                //val sfDocRef = querySnapshot.documents[0].reference

                val sfDocRef =  db.collection("warehouses").document(warehouseObject.value!!.warehouseID).collection("items").document(foundWhItem.warehouseItemID)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(sfDocRef)

                    var newCount: Double

                    if (addingMode) {//přidáváme
                        newCount = snapshot.getDouble("count")!! + count
                    } else {//odebíráme
                        newCount = snapshot.getDouble("count")!! - count

                    }

                    transaction.update(sfDocRef, "count", newCount)

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

                    //todo dořešit aby se event zkonzumoval už na skenneru
                    //přehraje animaci úspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlaySuccessAnimation) }

                    Log.d("Transakce", "Transaction success!")
                }.addOnFailureListener {
                    _loading.postValue(false)

                    //přehraje animaci neúspěchu
                    GlobalScope.launch { eventChannel.send(Event.PlayErrorAnimation) }

                    Log.d("Transakce", "Transaction failure!!!!!!!!!!" + it.message)
                }
            } catch (e: Exception) {
                _loading.postValue(false)
                //přehraje animaci neúspěchu
                GlobalScope.launch { eventChannel.send(Event.PlayErrorAnimation) }

                Log.d("EXCEPTION", e.message!!)
            }

        }


    }

    suspend fun getListOfActualWarehouseItems() {

        localListOfAllItemNames.clear()
        localListOfAllItemCodes.clear()
        localListOfAllItems.clear()


        allItemsInDb = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).collection("items").orderBy("name", Query.Direction.ASCENDING).get().await()
        for (document in allItemsInDb.documents) {
            localListOfAllItems.add(document.toObject(WarehouseItem::class.java)!!)
            localListOfAllItemNames.add(document.toObject(WarehouseItem::class.java)!!.name)
            localListOfAllItemCodes.add(document.toObject(WarehouseItem::class.java)!!.code)
        }


    }

    private fun returnWarehouseItemWithGivenParameters(itemName: String = "", itemBarcode: String = "", listOfAllWarehouseItems: MutableList<WarehouseItem>): WarehouseItem? {
        var foundObject: WarehouseItem? = null

        listOfAllWarehouseItems.any {
            if (it.name == itemName || it.code == itemBarcode) {
                foundObject = it
                Log.d("hajdin", "Nalezeno - Item:" + foundObject!!.name + " Code:" + foundObject!!.code)
                true //shoda našli jsme shodu podle jména nebo podle kódu
            } else false //neshoda nic jsme nenanšli
        }

        return foundObject

    }

}