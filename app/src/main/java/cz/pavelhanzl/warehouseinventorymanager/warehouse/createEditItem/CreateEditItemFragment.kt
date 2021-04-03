package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker

import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentCreateEditItemBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import kotlinx.coroutines.flow.onEach
import java.io.File

class CreateEditItemFragment : BaseFragment() {

    private val args: CreateEditItemFragmentArgs by navArgs()
    private lateinit var binding: FragmentCreateEditItemBinding
    lateinit var viewModel: CreateEditItemFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel = ViewModelProvider(this).get(CreateEditItemFragmentViewModel::class.java)
            viewModel.setdata(args.warehouseId)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentCreateEditItemBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        //pokud je předán objekt položky skladu v safeargs, tak jedeme v editmodu, jinak módu vytváření
        if(args.warehouseItemObject != null) runFragmentInEditMode() else runFragmentInCreateMode()


binding.ciItemProfileImageCreateEditItemFragment.setOnClickListener{

    ImagePicker.with(this)
        .cropOval()
        .cropSquare()	    			//Crop image(Optional), Check Customization for more option
        .compress(1024)			//Final image size will be less than 1 MB(Optional)
        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
        .start()
}


// Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    CreateEditItemFragmentViewModel.Event.NavigateBack -> {
                        findNavController().navigateUp()
                        hideKeyboard(activity as MainActivity)
                    }
                    CreateEditItemFragmentViewModel.Event.NavigatePopUpBackStackToWarehouseDetail -> {
                        findNavController().popBackStack(R.id.warehouseDetailFragment,false)
                        hideKeyboard(activity as MainActivity)
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }

    fun navigateToScanner() {
        val action = CreateEditItemFragmentDirections.actionCreateEditItemFragmentToScannerFragment(Constants.READING_STRING)
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            binding.ciItemProfileImageCreateEditItemFragment.setImageURI(fileUri)

            val inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            val byteArray = inputstream!!.readBytes()
            viewModel.itemProfilePhoto.value = byteArray


            //You can get File object from intent
            //val file: File = ImagePicker.getFile(data)!!



            //You can also get File Path from intent
            //val filePath:String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }




    private fun runFragmentInEditMode() {
        //nastaví editmode ve viewmodelu a vytvoří objekt editované položky skladu na základě skladu předáného v safeargs
        viewModel.editMode = true
        viewModel.editedWarehouseItem = args.warehouseItemObject!!

        //nastaví pole "název skladu" a "poznámka"
        viewModel.itemNameContent.postValue(args.warehouseItemObject!!.name)
        viewModel.itemBarcodeContent.postValue(args.warehouseItemObject!!.code)
        viewModel.itemPriceContent.postValue(args.warehouseItemObject!!.price.toString())
        viewModel.initialItemCountContent.postValue(args.warehouseItemObject!!.count.toString())
        viewModel.itemNoteContent.postValue(args.warehouseItemObject!!.note)


        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = getString(R.string.editItem)

        //změní text na vytvářejícím tllačítku na "upravit sklad"
        binding.btnCreateEditItemCreateEditItemFragment.text = getString(R.string.edit_warehouse)



        Glide.with(requireContext())
            .load(viewModel.editedWarehouseItem.photoURL)
            .placeholder(R.drawable.avatar_ownwarehouseavatar_secondary_color)
            .error(R.drawable.avatar_ownwarehouseavatar_secondary_color)
            .into(binding.ciItemProfileImageCreateEditItemFragment)

    }

    private fun runFragmentInCreateMode() {
        //nastaví editmode ve viewmodelu na false - vytváříme novou položku skladu
        viewModel.editMode = false

        //nastaví odpovídající title v actionbaru
        (activity as MainActivity).supportActionBar!!.title = resources.getString(R.string.CreateNewItem)

    }


}