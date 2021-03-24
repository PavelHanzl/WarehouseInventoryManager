package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import cz.pavelhanzl.warehouseinventorymanager.repository.User
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OwnWarehousesDetailFragmentViewModel : BaseViewModel() {
    var warehouseID = MutableLiveData<String>("")
    var warehouseObject = MutableLiveData<Warehouse>()
    lateinit var warehouseSnapshot: Map<String, Any>

    //odstraní sklad z databáze
    fun deleteWarehouse() {

        //běží v globalscope, aby bylo možné sklad obnovit ve funkci delete WhUndo i po zničení viewmodelu
        GlobalScope.launch(Dispatchers.IO) {
            warehouseSnapshot = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).get().await().data!!
            db.collection("warehouses").document(warehouseObject.value!!.warehouseID).delete()
        }
    }

    //obnoví smazaný sklad po kliku na snackbar
    fun deleteWarehouseUndo() {
        GlobalScope.launch(Dispatchers.IO) {
            db.collection("warehouses").document(warehouseObject.value!!.warehouseID).set(warehouseSnapshot)
        }
    }


    fun getWarehouseOwner(warehouseId: String): Task<DocumentSnapshot> {
        return db.collection("warehouses").document(warehouseId).get()
    }

    //nastaví data při creatu view k tomuto viewmodelu přiřazenému
    fun setData(warehouseId: String) {
        warehouseID.value = warehouseId

        val warehouseRef = db.collection("warehouses").document(warehouseId)
        warehouseRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("WH-Detail", "Listen failed.", error)
                return@addSnapshotListener
            }


            if (snapshot != null && snapshot.exists()) {
                Log.d("WH-Detail", "Current data: ${snapshot.data}")
                warehouseObject.value = snapshot.toObject<Warehouse>()!!
                Log.d("WH-Detail", "Current data: ${warehouseObject.value!!.name}")
            } else {
                Log.d("WH-Detail", "Current data: null")
            }

        }


    }

}