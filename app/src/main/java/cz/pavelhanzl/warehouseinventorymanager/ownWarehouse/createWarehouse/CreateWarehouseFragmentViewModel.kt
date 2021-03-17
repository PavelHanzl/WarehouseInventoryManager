package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse
import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.pavelhanzl.warehouseinventorymanager.repository.Warehouse
import cz.pavelhanzl.warehouseinventorymanager.service.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CreateWarehouseFragmentViewModel : BaseViewModel() {

    var warehouseNameContent = MutableLiveData<String>("")
    var warehouseNoteContent = MutableLiveData<String>("")
    var warehouseProfilePhoto = MutableLiveData<ByteArray>()



    private val _goBackToPreviousScreen = MutableLiveData<Boolean>(false)
    val goBackToPreviousScreen: LiveData<Boolean> get() = _goBackToPreviousScreen

    init {

    }

    @SuppressLint("SimpleDateFormat")
    fun onCreateButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Had", "Klikáš mi na vytvářecího hada, debile")

            try {
                var profileImageURL : Uri? = null
                val warehouseDocRef = db.collection("warehouses").document()


                if(warehouseProfilePhoto.value != null) {
                    Log.d("Had", "Snažíš se mi proběhnout, debile")
                    try {
                        storage.child("/warehouses/${warehouseDocRef.id}/profileImage.jpg").putBytes(warehouseProfilePhoto.value!!).await()
                        profileImageURL = storage.child("/warehouses/${warehouseDocRef.id}/profileImage.jpg").downloadUrl.await()
                    } catch (e: Exception){
                        Log.d("Uložto", e.message.toString())
                    }

                }


                val warehouse = Warehouse()
                warehouse.warehouseID = warehouseDocRef.id
                warehouse.owner = auth.currentUser!!.uid
                warehouse.name = warehouseNameContent.value!!
                warehouse.note = warehouseNoteContent.value!!
                warehouse.photoURL = profileImageURL?.toString() ?: ""

                warehouseDocRef.set(warehouse).await()

                repoComunicationLayer.createWarehouseLogItem("Sklad vytvořen!",warehouseID = warehouseDocRef.id)


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