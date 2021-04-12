package cz.pavelhanzl.warehouseinventorymanager.warehouse.createEditItem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

/**
 * Create edit item fragment
 *
 * @constructor Create empty Create edit item fragment
 */
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

            //pokud jsme na skenneru naskenovali nějakou položku co ještě není na skladě, tak předáváme v safe args jeji barcode a chceme jej tedy předvyplnit
            if(args.scannedBarcodeValue != null) viewModel.itemBarcodeContent.value = args.scannedBarcodeValue

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binduje a přiřazuje viewmodel
        binding = FragmentCreateEditItemBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.fragmentClass = this
        binding.lifecycleOwner = viewLifecycleOwner

        //pokud je předán objekt položky skladu v safeargs, tak jedeme v editmodu, jinak módu vytváření
        if (args.warehouseItemObject != null) runFragmentInEditMode() else runFragmentInCreateMode()


        binding.ciItemProfileImageCreateEditItemFragment.setOnClickListener {

            ImagePicker.with(this)
                .cropOval()
                .cropSquare()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.tfItemNameContentCreateEditItemFragment.doAfterTextChanged {
            viewModel.checkIfThereIsNoItemWithSameNameInWH(it.toString())
        }

        binding.tfItemBarcodeContentCreateEditItemFragment.doAfterTextChanged {
            viewModel.checkIfThereIsNoItemWithSameBarcodeInWH(it.toString())
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
                        findNavController().popBackStack(R.id.warehouseDetailFragment, false)
                        hideKeyboard(activity as MainActivity)
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)

        registerObserverForResultOfScanner()
    }

    /**
     * Navigates to scanner
     */
    fun navigateToScanner() {
        val action = CreateEditItemFragmentDirections.actionCreateEditItemFragmentToScannerFragment(Constants.READING_STRING,null)
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
            Toast.makeText(requireContext(),  getString(R.string.operationCanceled), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Runs fragment in edit mode
     *
     */
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
        viewModel.itemPhotoUrl.postValue(args.warehouseItemObject!!.photoURL)

        //nastaví odpovídající title v actionbaru "Ostatní sklady"
        (activity as MainActivity).supportActionBar!!.title = getString(R.string.editItem)

        //zamezí uživateli měnit počet, k tomu by měl využít standartní funkčnost aplikace, aby došlo k vytvoření logu + nastaví odpovídající hint
        binding.tfInitialItemCountCreateEditItemFragment.hint = getString(R.string.numberOfPcsInWarehouse)

        //replikuje funkčnost isEnabled=false, ale dovolí připojit onclick listener
        binding.tfInitialItemCountContentCreateEditItemFragment.isFocusable = false
        binding.tfInitialItemCountContentCreateEditItemFragment.isLongClickable = false
        binding.tfInitialItemCountContentCreateEditItemFragment.isCursorVisible = false

        binding.tfInitialItemCountContentCreateEditItemFragment.setOnClickListener {
            Toast.makeText(requireContext(),getString(R.string.youCanNotChangeCountInEditMode), Toast.LENGTH_LONG).show()
        }


        Glide.with(requireContext())
            .load(viewModel.editedWarehouseItem.photoURL)
            .placeholder(R.drawable.avatar_ownwarehouseavatar_secondary_color)
            .error(R.drawable.avatar_ownwarehouseavatar_secondary_color)
            .into(binding.ciItemProfileImageCreateEditItemFragment)

    }

    /**
     * Runs fragment in create mode
     *
     */
    private fun runFragmentInCreateMode() {
        //nastaví editmode ve viewmodelu na false - vytváříme novou položku skladu
        viewModel.editMode = false

        //nastaví odpovídající title v actionbaru
        (activity as MainActivity).supportActionBar!!.title = resources.getString(R.string.CreateNewItem)

    }

    /**
     * Register observer for result of scanner
     * Ensures the passing of the argument from the previous activity (in this case it gets the result if it comes from the scanner)
     */
    private fun registerObserverForResultOfScanner() {  //zajišťuje předání argumentu z předchozí aktivity (v tomto případě získá result pokud přichází ze skenneru)
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.createEditItemFragment)

        // Create observer and add it to the NavBackStackEntry's lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                && navBackStackEntry.savedStateHandle.contains("scannedBarcode")
            ) {
                val result = navBackStackEntry.savedStateHandle.get<String>("scannedBarcode")

                // Uloží předaný argument ze skenneru do proměnné barcode v sharedviewmodelu
                viewModel.itemBarcodeContent.postValue(result)
                viewModel.checkIfThereIsNoItemWithSameBarcodeInWH(result.toString())

            }
        }
        navBackStackEntry.lifecycle.addObserver(observer)

        // As addObserver() does not automatically remove the observer, we
        // call removeObserver() manually when the view lifecycle is destroyed
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        })
    }

}