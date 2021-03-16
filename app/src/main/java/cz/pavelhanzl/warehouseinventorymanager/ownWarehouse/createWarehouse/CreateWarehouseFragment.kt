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
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateWarehouseBinding
import kotlinx.android.synthetic.main.fragment_create_warehouse.*

class CreateWarehouseFragment : Fragment() {
    private lateinit var binding: FragmentCreateWarehouseBinding
    lateinit var viewModel: CreateWarehouseFragmentViewModel
    var imageFile: Uri? = null
    lateinit var obrazekDrawable: Drawable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_warehouse, container, false)
        viewModel = ViewModelProvider(this).get(CreateWarehouseFragmentViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.goBackToPreviousScreen.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigateUp()
            }
        })


        binding.ciWarehouseProfileImageFragmentCreateWarehouse.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, 0)
            }
        }


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( resultCode == Activity.RESULT_OK && requestCode == 0){
            data?.data.let {
                imageFile =it
                ci_WarehouseProfileImage_FragmentCreateWarehouse.setImageURI(imageFile)
                var inputstream = requireContext().contentResolver.openInputStream(it!!)
                var bytove = inputstream!!.readBytes()
                viewModel.warehouseProfilePhoto.value = bytove
            }
        }
    }

    override fun onDestroy() {
        Log.d("Ničitel","Myších doupat!")
        super.onDestroy()

    }

}