package cz.pavelhanzl.warehouseinventorymanager

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class About : Fragment() {
    var storageRef = Firebase.storage.reference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        CoroutineScope(Dispatchers.IO).launch{
        var imageRef = storageRef.child("/avatar.png").getBytes(5L*1024*1024).await()
        var bmp =BitmapFactory.decodeByteArray(imageRef, 0, imageRef.size)
        withContext(Dispatchers.Main){
            imageView.setImageBitmap(bmp)
        }

        }
        return view
    }


}