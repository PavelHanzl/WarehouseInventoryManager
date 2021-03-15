package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CreateWarehouseFragmentViewModel : BaseViewModel() {

    var warehouseNameContent = MutableLiveData<String>("")
    var warehouseNoteContent = MutableLiveData<String>("")

    private val _photoURL = MutableLiveData<String>("")
    val photoURL: LiveData<String> get() = _photoURL

    private val _showCreateAnim = MutableLiveData<Boolean>(false)
    val showCreateAnim: LiveData<Boolean> get() = _showCreateAnim

    private val _goBackToPreviousScreen = MutableLiveData<Boolean>(false)
    val goBackToPreviousScreen: LiveData<Boolean> get() = _goBackToPreviousScreen

    init {

    }

    fun onCreateButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Had", "Klikáš mi na vytvářecího hada, debile")

            try {
                val ref = db.collection("warehouses").document()

                val warehouse: MutableMap<String, Any> = HashMap()
                warehouse["warehouseID"] = ref.id
                warehouse["owner"] = auth.currentUser!!.uid
                warehouse["name"] = warehouseNameContent.value!!
                warehouse["note"] = warehouseNoteContent.value!!
                warehouse["photoURL"] = ""

                ref.set(warehouse).await()
            } catch (e: Exception) {
                Log.d("Had", "Něco je špatně: ${e.message}")
                return@launch
            }
           _goBackToPreviousScreen.postValue(true)
        }
    }

    fun onBackButtonClicked() {
        Log.d("Had", "Klikáš mi na zpětného hada, debile")
        _goBackToPreviousScreen.postValue(true)
    }
}