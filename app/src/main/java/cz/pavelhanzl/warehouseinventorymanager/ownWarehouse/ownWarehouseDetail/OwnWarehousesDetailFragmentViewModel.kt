package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.ownWarehouseDetail

import android.util.Log
import androidx.lifecycle.MutableLiveData
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

    fun deleteWarehouse() {
        GlobalScope.launch(Dispatchers.IO) {
            warehouseSnapshot = db.collection("warehouses").document(warehouseObject.value!!.warehouseID).get().await().data!!
            db.collection("warehouses").document(warehouseObject.value!!.warehouseID).delete()
        }
    }

    fun deleteWarehouseUndo() {
        GlobalScope.launch(Dispatchers.IO) {
            db.collection("warehouses").document(warehouseObject.value!!.warehouseID).set(warehouseSnapshot)
        }
    }

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