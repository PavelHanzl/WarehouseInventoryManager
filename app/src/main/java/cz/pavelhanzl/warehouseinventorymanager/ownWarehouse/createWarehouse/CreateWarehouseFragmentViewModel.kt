package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import cz.pavelhanzl.warehouseinventorymanager.R
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
    var warehouseProfilePhoto = MutableLiveData<ByteArray>()

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

                val warehouse: MutableMap<String, Any> = HashMap()
                warehouse["warehouseID"] = warehouseDocRef.id
                warehouse["owner"] = auth.currentUser!!.uid
                warehouse["name"] = warehouseNameContent.value!!
                warehouse["note"] = warehouseNoteContent.value!!
                warehouse["photoURL"] = if (profileImageURL!=null) profileImageURL.toString() else ""

                warehouseDocRef.set(warehouse).await()
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