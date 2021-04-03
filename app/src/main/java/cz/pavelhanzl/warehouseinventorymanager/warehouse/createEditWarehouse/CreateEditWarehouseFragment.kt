package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditWarehouse

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateEditWarehouseBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import kotlinx.android.synthetic.main.fragment_create_edit_warehouse.*

class CreateEditWarehouseFragment : Fragment() {
    private lateinit var binding: FragmentCreateEditWarehouseBinding
    lateinit var viewModel: CreateEditWarehouseFragmentViewModel
    private val args: CreateEditWarehouseFragmentArgs by navArgs()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        viewModel = ViewModelProvider(this).get(CreateEditWarehouseFragmentViewModel::class.java)
        binding = FragmentCreateEditWarehouseBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //pokud je předán objekt skladu v safeargs, tak jedeme v editmodu, jinak módu vytváření
        if(args.warehouseObject != null) runFragmentInEditMode() else runFragmentInCreateMode()


        viewModel.goBackToPreviousScreen.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigateUp()
                hideKeyboard(activity as MainActivity)
            }
        })


        binding.ciWarehouseProfileImageFragmentCreateWarehouse.setOnClickListener {
            ImagePicker.with(this)
                .cropOval()
                .cropSquare() //Crop image(Optional), Check Customization for more option
                .compress(1024)	//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }



        // Inflate the layout for this fragment
        return binding.root
    }

    private fun runFragmentInEditMode() {
        //nastaví editmode ve viewmodelu a vytvoří objekt editovaného skladu na základě skladu předáného v safeargs
        viewModel.editMode = true
        viewModel.edittedWarehouse = args.warehouseObject!!

        //nastaví pole "název skladu" a "poznámka"
        viewModel.warehouseNameContent.postValue(args.warehouseObject!!.name)
        viewModel.warehouseNoteContent.postValue(args.warehouseObject!!.note)


        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = "Upravit informace o skladu"

        //změní text na vytvářejícím tllačítku na "upravit sklad"
        binding.btnCreateWarehouseFragmentCreateWarehouse.text = getString(R.string.edit_warehouse)



        Glide.with(requireContext())
            .load(viewModel.edittedWarehouse.photoURL)
            .placeholder(R.drawable.avatar_ownwarehouseavatar_secondary_color)
            .error(R.drawable.avatar_ownwarehouseavatar_secondary_color)
            .into(binding.ciWarehouseProfileImageFragmentCreateWarehouse)

    }

    private fun runFragmentInCreateMode() {
        //nastaví editmode ve viewmodelu na false - vytváříme nový sklad
        viewModel.editMode = false

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = resources.getString(R.string.CreateNewWarehouse)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {

            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data

            //nastaví obrázek
            ci_WarehouseProfileImage_FragmentCreateWarehouse.setImageURI(fileUri)

            var inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            var byteArray = inputstream!!.readBytes()
            viewModel.warehouseProfilePhoto.value = byteArray

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.Canceled), Toast.LENGTH_SHORT).show()
        }

    }



}