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
import com.github.drjacky.imagepicker.ImagePicker
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
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




binding.ciItemProfileImageAddRemoveFragment.setOnClickListener{

    ImagePicker.with(this)
        .cropSquare()	    			//Crop image(Optional), Check Customization for more option
        .compress(1024)			//Final image size will be less than 1 MB(Optional)
        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
        .start()
}



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
            binding.ciItemProfileImageAddRemoveFragment.setImageURI(fileUri)

            var inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            var byteArray = inputstream!!.readBytes()
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

}