package cz.pavelhanzl.warehouseinventorymanager.ownWarehouse.createWarehouse

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateWarehouseBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_create_warehouse.*

class CreateWarehouseFragment : Fragment() {
    private lateinit var binding: FragmentCreateWarehouseBinding
    lateinit var viewModel: CreateWarehouseFragmentViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_warehouse, container, false)
        viewModel = ViewModelProvider(this).get(CreateWarehouseFragmentViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.goBackToPreviousScreen.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigateUp()
                hideKeyboard(activity as MainActivity)
            }
        })


        binding.ciWarehouseProfileImageFragmentCreateWarehouse.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare() //Crop image(Optional), Check Customization for more option
                .compress(1024)	//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }



        // Inflate the layout for this fragment
        return binding.root
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //zobrazí loading overlay
        (activity as MainActivity).showLoading()

        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {

            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data

            //nastaví obrá
            ci_WarehouseProfileImage_FragmentCreateWarehouse.setImageURI(fileUri)

            var inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            var byteArray = inputstream!!.readBytes()
            viewModel.warehouseProfilePhoto.value = byteArray

//            You can get File object from intent
//            val file:File = ImagePicker.getFile(data)!!
//
//            You can also get File Path from intent
//            val filePath:String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.Canceled), Toast.LENGTH_SHORT).show()
        }

        //skryje loading overlay
        (activity as MainActivity).hideLoading()
    }

    // skryje probíhající loading overlay pokud uživatel zmáčkne zpět a nestihne se dokončit skrytí
    override fun onDestroy() {
        (activity as MainActivity).hideLoading()
        super.onDestroy()
    }


}