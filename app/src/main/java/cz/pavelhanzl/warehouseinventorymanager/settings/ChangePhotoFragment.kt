package cz.pavelhanzl.warehouseinventorymanager.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentChangePasswordBinding
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentChangePhotoBinding
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import kotlinx.coroutines.flow.onEach

class ChangePhotoFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentChangePhotoBinding
    private val viewModel: SettingsFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentChangePhotoBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.fragmentClass = this
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel.getUsersPhoto()
        viewModel.wipeData()

        // Inflate the layout for this fragment
        return binding.root
    }

    fun runCamera() {
        ImagePicker.with(this)
            .cropOval()
            .cropSquare()                    //Crop image(Optional), Check Customization for more option
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            binding.ciItemProfileImageFragmentChangePhoto.setImageURI(fileUri)

            val inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            val byteArray = inputstream!!.readBytes()
            viewModel.userProfilePhoto.value = byteArray

            //You can get File object from intent
            //val file: File = ImagePicker.getFile(data)!!

            //You can also get File Path from intent
            //val filePath:String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.operationCanceled), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    SettingsFragmentViewModel.Event.NavigateBack -> {
                        findNavController().navigateUp()
                        Toast.makeText(context, getString(R.string.photoWasChanged), Toast.LENGTH_LONG).show()
                        hideKeyboard(activity as MainActivity)
                    }

                    SettingsFragmentViewModel.Event.NoPhotoSelected -> {
                        Toast.makeText(context, getString(R.string.NoPhotoSelected), Toast.LENGTH_LONG).show()
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }

}